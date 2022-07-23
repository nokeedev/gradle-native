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

import dev.nokee.language.base.internal.HasConfigurableSourceMixIn;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.util.PatternFilterable;

@DomainObjectEntities.Tag({JavaSourceSetSpec.Tag.class, ConfigurableTag.class, IsLanguageSourceSet.class, JvmSourceSetTag.class})
public /*final*/ class JavaSourceSetSpec implements JavaSourceSet, HasPublicType, ModelBackedLanguageSourceSetLegacyMixIn<JavaSourceSet>, HasConfigurableSourceMixIn {
	public TaskProvider<JavaCompile> getCompileTask() {
		return (TaskProvider<JavaCompile>) ModelElements.of(this).element("compile", JavaCompile.class).asProvider();
	}

	@Override
	public JavaSourceSet from(Object... paths) {
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
	public JavaSourceSet convention(Object... path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(JavaSourceSet.class);
	}

	public interface Tag extends ModelTag {}
}
