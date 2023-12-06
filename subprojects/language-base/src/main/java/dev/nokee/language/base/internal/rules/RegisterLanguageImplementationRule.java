/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.language.base.internal.rules;

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.language.base.internal.Implements;
import dev.nokee.language.base.internal.LanguageSupportSpec;
import org.gradle.api.Action;

public final class RegisterLanguageImplementationRule implements Action<LanguageSupportSpec> {
	private final Instantiator instantiator;

	public RegisterLanguageImplementationRule(Instantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public void execute(LanguageSupportSpec target) {
		for (Implements languageImplementations : target.getClass().getAnnotationsByType(Implements.class)) {
			target.getLanguageImplementations().add(instantiator.newInstance(languageImplementations.value()));
		}
	}
}
