/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class DomainObjectProviderMapTester<T> extends AbstractDomainObjectProviderTransformTester<T> {
	@Override
	protected <S> Provider<S> transform(DomainObjectProvider<T> provider, Transformer<S, T> mapper) {
		return provider.map(mapper);
	}

	@Test
	void throwsExceptionIfTransformerIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().map(null));
	}
}
