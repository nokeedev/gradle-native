/*
 * Copyright 2020-2021 the original author or authors.
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
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractDomainObjectProviderTransformTester<T> extends AbstractDomainObjectProviderTester<T> {
	protected abstract <S> Provider<S> transform(DomainObjectProvider<T> provider, Transformer<S, T> mapper);

	@Test
	void doesNotResolveProvider() {
		@SuppressWarnings("unchecked")
		val mapper = (Transformer<?, T>) mock(Transformer.class);
		val provider = createSubject();
		transform(provider, mapper);
		expectUnresolved(provider);
	}

	@Test
	void doesNotCallTransformerWhenResolvingBaseProvider() {
		@SuppressWarnings("unchecked")
		val mapper = (Transformer<?, T>) mock(Transformer.class);
		val provider = createSubject();
		transform(provider, mapper);

		when:
		resolve(provider);

		then:
		verify(mapper, never()).transform(any());
	}

	@Test
	void resolvesBaseProviderWhenResolvingMappedProvider() {
		val provider = createSubject();
		val mappedProvider = transform(provider, noOpTransformer());

		when:
		resolve(mappedProvider);

		then:
		expectResolved(provider);
	}
}
