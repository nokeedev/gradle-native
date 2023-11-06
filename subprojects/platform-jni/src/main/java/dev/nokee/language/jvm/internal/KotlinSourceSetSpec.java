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
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import lombok.val;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternFilterable;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

import static dev.nokee.model.internal.ModelObjectIdentifiers.asFullyQualifiedName;
import static java.util.Objects.requireNonNull;

@DomainObjectEntities.Tag({KotlinSourceSetSpec.Tag.class, ConfigurableTag.class, IsLanguageSourceSet.class, JvmSourceSetTag.class})
public /*final*/ abstract class KotlinSourceSetSpec extends ModelElementSupport implements KotlinSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<KotlinSourceSet>, HasSource {
	@Inject
	public KotlinSourceSetSpec(NamedDomainObjectCollection<SourceSet> sourceSets) {
		NamedDomainObjectProvider<SourceSet> sourceSetProvider = sourceSets.named(asFullyQualifiedName(requireNonNull(getIdentifier().getParent())).toString());
		getSource().from(sourceSetProvider.map(KotlinSourceSetSpec::asSourceDirectorySet));
		getSource().disallowChanges();
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
	public TaskProvider<? extends Task> getCompileTask() {
		return (TaskProvider<Task>) ModelElements.of(this).element("compile", Task.class).asProvider();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return getSource().getBuildDependencies();
	}

	@Override
	public KotlinSourceSet from(Object... paths) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFrom(Object... paths) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PatternFilterable getFilter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public KotlinSourceSet convention(Object... path) {
		throw new UnsupportedOperationException();
	}

	public interface Tag extends ModelTag {}
}
