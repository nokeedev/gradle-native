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
package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LegacySourceSetTag;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.model.internal.core.ModelRegistration;

import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class ObjectiveCppSourceSetRegistrationFactory {
	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		return create(identifier, false);
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier, boolean isLegacy) {
		if (isLegacy) {
			return ModelRegistration.managedBuilder(identifier, LegacyObjectiveCppSourceSet.class).withComponent(tag(LegacySourceSetTag.class)).build();
		} else {
			return ModelRegistration.managedBuilder(identifier, ObjectiveCppSourceSetSpec.class).withComponent(new NativeCompileTypeComponent(ObjectiveCppCompileTask.class)).build();
		}
	}
}
