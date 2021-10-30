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
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.internal.AttachHeaderSearchPathsToCompileTaskRule;
import dev.nokee.language.nativebase.internal.HeaderSearchPathsConfigurationRegistrationActionFactory;
import dev.nokee.language.nativebase.internal.HeadersPropertyRegistrationActionFactory;
import dev.nokee.language.nativebase.internal.NativeCompileTaskRegistrationActionFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import lombok.val;

public final class CppSourceSetRegistrationFactory {
	private final LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory;
	private final HeadersPropertyRegistrationActionFactory headersPropertyFactory;
	private final HeaderSearchPathsConfigurationRegistrationActionFactory resolvableHeadersRegistrationFactory;
	private final NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory;

	public CppSourceSetRegistrationFactory(LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory, HeadersPropertyRegistrationActionFactory headersPropertyFactory, HeaderSearchPathsConfigurationRegistrationActionFactory resolvableHeadersRegistrationFactory, NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory) {
		this.sourceSetRegistrationFactory = sourceSetRegistrationFactory;
		this.headersPropertyFactory = headersPropertyFactory;
		this.resolvableHeadersRegistrationFactory = resolvableHeadersRegistrationFactory;
		this.compileTaskRegistrationFactory = compileTaskRegistrationFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		return create(identifier, true);
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier, boolean isLegacy) {
		val builder = sourceSetRegistrationFactory.create(identifier, CppSourceSet.class, DefaultCppSourceSet.class);
		if (!isLegacy) {
			builder.action(headersPropertyFactory.create(identifier))
				.action(compileTaskRegistrationFactory.create(identifier, CppCompile.class, CppCompileTask.class))
				.action(resolvableHeadersRegistrationFactory.create(identifier))
				.action(new AttachHeaderSearchPathsToCompileTaskRule(identifier));
		}
		return builder.build();
	}

	public static class DefaultCppSourceSet implements CppSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<CppSourceSet> {}
}
