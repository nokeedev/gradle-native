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
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.GroovyCompile;

import javax.inject.Inject;

import static dev.nokee.model.internal.ModelObjectIdentifiers.asFullyQualifiedName;
import static dev.nokee.utils.TaskDependencyUtils.of;
import static java.util.Objects.requireNonNull;

public /*final*/ abstract class GroovySourceSetSpec extends ModelElementSupport implements GroovySourceSet
	, HasSource {
	@Inject
	public GroovySourceSetSpec(NamedDomainObjectCollection<SourceSet> sourceSets, ModelObjectRegistry<Task> taskRegistry) {
		final NamedDomainObjectProvider<SourceSet> sourceSetProvider = sourceSets.named(asFullyQualifiedName(requireNonNull(getIdentifier().getParent())).toString());
		getSource().setFrom(sourceSetProvider.map(GroovySourceSetSpec::asSourceDirectorySet));
		getSource().disallowChanges();

		getExtensions().add("compileTask", taskRegistry.register(getIdentifier().child(TaskName.of("compile")), GroovyCompile.class).asProvider());
	}

	private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
		return ((org.gradle.api.tasks.GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy")).getGroovy();
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskProvider<GroovyCompile> getCompileTask() {
		return (TaskProvider<GroovyCompile>) getExtensions().getByName("compileTask");
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), of(getCompileTask()));
	}

	@Override
	protected String getTypeName() {
		return "Groovy sources";
	}
}
