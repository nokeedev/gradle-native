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
package dev.nokee.model.internal;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

final class ValueProvidedImpl<T> implements Value<T> {
	private final NamedDomainObjectProvider<T> provider;

	public ValueProvidedImpl(NamedDomainObjectProvider<T> provider) {
		this.provider = provider;
	}

	@Override
	public T get() {
		return provider.get();
	}

	@Override
	public Class<T> getType() {
		return ProviderUtils.getType(provider).orElse(null);
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> mapper) {
		return provider.map(mapper);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper) {
		return provider.flatMap(mapper);
	}

	@Override
	public Value<T> mapInPlace(Transformer<? extends T, ? super T> mapper) {
		provider.configure(mapper::transform);
		return this;
	}

	@Override
	public Provider<T> toProvider() {
		return provider;
	}
}
