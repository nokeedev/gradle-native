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
package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BinaryIdentifier;

import javax.inject.Provider;

public final class KnownBinaryFactory implements KnownDomainObjectFactory<Binary> {
	private final Provider<BinaryRepository> repositoryProvider;
	private final Provider<BinaryConfigurer> configurerProvider;

	public KnownBinaryFactory(Provider<BinaryRepository> repositoryProvider, Provider<BinaryConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Binary> KnownBinary<T> create(BinaryIdentifier<T> identifier) {
		return new KnownBinary<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Binary> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((BinaryIdentifier<S>)identifier);
	}
}
