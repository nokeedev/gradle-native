/*
 * Copyright 2022 the original author or authors.
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
package dev.gradleplugins.dockit.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.javadoc.Javadoc;

public final class JavadocAdditionalArgsOption implements Action<Javadoc> {
	private static final String ADDITIONAL_ARGS_EXTENSION_NAME = "additionalArgs";
	private final Project project;

	JavadocAdditionalArgsOption(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Javadoc task) {
		new JavadocPropertyFactory(project).named(ADDITIONAL_ARGS_EXTENSION_NAME).apply(task);
	}

	@SuppressWarnings("unchecked")
	public static ListProperty<String> additionalArgs(Javadoc self) {
		return (ListProperty<String>) self.getExtensions().getByName(ADDITIONAL_ARGS_EXTENSION_NAME);
	}
}
