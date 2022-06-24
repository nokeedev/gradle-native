/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.jni.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;

public final class ConfigureJniHeaderDirectoryOnJavaCompileAction implements Action<JavaCompile> {
	private final DomainObjectIdentifier identifier;
	private final ProjectLayout layout;

	public ConfigureJniHeaderDirectoryOnJavaCompileAction(DomainObjectIdentifier identifier, ProjectLayout layout) {
		this.identifier = identifier;
		this.layout = layout;
	}

	@Override
	public void execute(JavaCompile task) {
		task.getOptions().getHeaderOutputDirectory().convention(layout.getBuildDirectory().dir("generated/jni-headers/" + OutputDirectoryPath.fromIdentifier(identifier)));

		// The nested output is not marked automatically as an output of the task regarding task dependencies.
		// So we mark it manually here.
		// See https://github.com/gradle/gradle/issues/6619.
		if (!isGradleVersionGreaterOrEqualsTo6Dot3()) {
			task.getOutputs().dir(task.getOptions().getHeaderOutputDirectory());
		}

		// Cannot do incremental header generation before 6.3, since the pattern for cleaning them up is currently wrong.
		// See https://github.com/gradle/gradle/issues/12084.
		task.getOptions().setIncremental(isGradleVersionGreaterOrEqualsTo6Dot3());
	}

	private static boolean isGradleVersionGreaterOrEqualsTo6Dot3() {
		return GradleVersion.current().compareTo(GradleVersion.version("6.3")) >= 0;
	}
}
