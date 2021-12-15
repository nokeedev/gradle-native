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
package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.internal.AttachHeaderSearchPathsToCompileTaskRule;
import dev.nokee.language.nativebase.internal.NativeCompileTaskDefaultConfigurationRule;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageTag;
import dev.nokee.language.nativebase.internal.NativeSourceSetLegacyTag;
import dev.nokee.model.internal.core.ModelRegistration;
import lombok.val;

public final class CppSourceSetRegistrationFactory {
	private final LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory;

	public CppSourceSetRegistrationFactory(LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory) {
		this.sourceSetRegistrationFactory = sourceSetRegistrationFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		return create(identifier, false);
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier, boolean isLegacy) {
		val builder = sourceSetRegistrationFactory.create(identifier);
		builder.withComponent(CppSourceSetTag.INSTANCE);
		if (isLegacy) {
			builder.withComponent(NativeSourceSetLegacyTag.INSTANCE);
		} else {
			builder.withComponent(NativeHeaderLanguageTag.INSTANCE)
				.action(new AttachHeaderSearchPathsToCompileTaskRule(identifier))
				.action(new NativeCompileTaskDefaultConfigurationRule(identifier))
			;
		}
		return builder.build();
	}


}
