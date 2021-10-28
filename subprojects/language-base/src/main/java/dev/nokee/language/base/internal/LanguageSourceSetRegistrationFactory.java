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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class LanguageSourceSetRegistrationFactory {
	private final ObjectFactory objectFactory;

	public LanguageSourceSetRegistrationFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public <T extends LanguageSourceSet> LanguageSourceSetRegistrationBuilder create(LanguageSourceSetIdentifier identifier, Class<T> publicType) {
		return new LanguageSourceSetRegistrationBuilder(identifier, createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(publicType)));
	}

	public <T extends LanguageSourceSet> LanguageSourceSetRegistrationBuilder create(LanguageSourceSetIdentifier identifier, Class<? super T> publicType, Class<T> implementationType) {
		return new LanguageSourceSetRegistrationBuilder(identifier, createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(implementationType)));
	}
}
