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

import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Streams;
import dev.nokee.language.base.HasSource;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.type.ModelTypeUtils;
import dev.nokee.model.internal.names.TaskName;
import lombok.val;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

import static dev.nokee.model.internal.ModelObjectIdentifiers.asFullyQualifiedName;
import static java.util.Objects.requireNonNull;

public /*final*/ abstract class KotlinSourceSetSpec extends ModelElementSupport implements KotlinSourceSet
	, HasSource {
	@Inject
	public KotlinSourceSetSpec(NamedDomainObjectCollection<SourceSet> sourceSets, ModelObjectRegistry<Task> taskRegistry, TaskContainer tasks) {
		final NamedDomainObjectProvider<SourceSet> sourceSetProvider = sourceSets.named(asFullyQualifiedName(requireNonNull(getIdentifier().getParent())).toString());
		getSource().from(sourceSetProvider.map(KotlinSourceSetSpec::asSourceDirectorySet));
		getSource().disallowChanges();

		@SuppressWarnings("unchecked")
		final Class<Task> KotlinCompile  = (Class<Task>) ModelTypeUtils.toUndecoratedType(sourceSetProvider.map(it -> Streams.stream(tasks.getCollectionSchema().getElements()).filter(t -> t.getName().equals(it.getCompileTaskName("kotlin"))).map(t -> t.getPublicType().getConcreteClass()).collect(MoreCollectors.onlyElement())).get());
		getExtensions().add("compileTask", taskRegistry.register(getIdentifier().child(TaskName.of("compile")), KotlinCompile).asProvider());
	}

	private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
		try {
			val kotlinSourceSet = new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
			val DefaultKotlinSourceSet = kotlinSourceSet.getClass();
			val getKotlin = DefaultKotlinSourceSet.getMethod("getKotlin");
			return (SourceDirectorySet) getKotlin.invoke(kotlinSourceSet);
		} catch (NoSuchMethodException | IllegalAccessException |
				 InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskProvider<? extends Task> getCompileTask() {
		return (TaskProvider<Task>) getExtensions().getByName("compileTask");
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return getSource().getBuildDependencies();
	}

	@Override
	protected String getTypeName() {
		return "Kotlin sources";
	}
}
