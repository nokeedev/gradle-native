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
package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.VariantIdentifier;

import javax.inject.Provider;

public final class KnownVariantFactory implements KnownDomainObjectFactory<Variant> {
	private final Provider<VariantRepository> repositoryProvider;
	private final Provider<VariantConfigurer> configurerProvider;

	public KnownVariantFactory(Provider<VariantRepository> repositoryProvider, Provider<VariantConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Variant> KnownVariant<T> create(VariantIdentifier<T> identifier) {
		return new KnownVariant<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Variant> KnownVariant<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((VariantIdentifier<S>)identifier);
	}
}
