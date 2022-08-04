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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.HasConfigurableSourceMixIn;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTag;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersMixIn;
import dev.nokee.language.nativebase.internal.HasNativeCompileTaskMixIn;
import dev.nokee.language.nativebase.internal.NativeHeaderSetTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;

@DomainObjectEntities.Tag({CSourceSetTag.class, CSourceSetSpec.Tag.class, ConfigurableTag.class, IsLanguageSourceSet.class, NativeHeaderSetTag.class})
public /*final*/ class CSourceSetSpec implements CSourceSet, HasPublicType, ModelBackedLanguageSourceSetLegacyMixIn<CSourceSet>, HasConfigurableSourceMixIn, HasConfigurableHeadersMixIn, HasNativeCompileTaskMixIn<CCompileTask> {
	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), getHeaders().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(CSourceSet.class);
	}

	@Override
	public String toString() {
		return "C sources '" + getName() + "'";
	}

	public interface Tag extends ModelTag {}
}
