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
import dev.nokee.language.base.internal.LegacySourceSetTag;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;

@DomainObjectEntities.Tag({CSourceSetTag.class, LegacySourceSetTag.class, ConfigurableTag.class, IsLanguageSourceSet.class})
public /*final*/ class LegacyCSourceSet implements CSourceSet, HasPublicType, ModelBackedLanguageSourceSetLegacyMixIn<CSourceSet>, HasConfigurableSourceMixIn {
	@Override
	public TaskDependency getBuildDependencies() {
		return getSource().getBuildDependencies();
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(CSourceSet.class);
	}

	@Override
	public String toString() {
		return "C sources '" + getName() + "'";
	}
}
