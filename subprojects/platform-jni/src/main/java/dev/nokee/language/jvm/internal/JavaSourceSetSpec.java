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
package dev.nokee.language.jvm.internal;

import dev.nokee.language.base.HasSource;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;

import static dev.nokee.model.internal.ModelObjectIdentifiers.asFullyQualifiedName;
import static java.util.Objects.requireNonNull;

public /*final*/ abstract class JavaSourceSetSpec extends ModelElementSupport implements JavaSourceSet
	, HasSource {
	@Inject
	public JavaSourceSetSpec(NamedDomainObjectCollection<SourceSet> sourceSets, ModelObjectRegistry<Task> taskRegistry) {
		final NamedDomainObjectProvider<SourceSet> sourceSetProvider = sourceSets.named(asFullyQualifiedName(requireNonNull(getIdentifier().getParent())).toString());
		getSource().from(sourceSetProvider.map(JavaSourceSetSpec::asSourceDirectorySet));
		getSource().disallowChanges();

		getExtensions().add("compileTask", taskRegistry.register(getIdentifier().child(TaskName.of("compile")), JavaCompile.class).asProvider());
	}

	private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
		return sourceSet.getJava();
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskProvider<JavaCompile> getCompileTask() {
		return (TaskProvider<JavaCompile>) getExtensions().getByName("compileTask");
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}
}
