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
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public abstract class AbstractKnownDomainObject<TYPE, T extends TYPE> implements KnownDomainObject<T> {
	private final TypeAwareDomainObjectIdentifier<T> identifier;
	@EqualsAndHashCode.Exclude private final Provider<T> provider;
	@EqualsAndHashCode.Exclude private final DomainObjectConfigurer<TYPE> configurer;

	protected AbstractKnownDomainObject(TypeAwareDomainObjectIdentifier<T> identifier, Provider<T> provider, DomainObjectConfigurer<TYPE> configurer) {
		this.identifier = requireNonNull(identifier);
		this.provider = requireNonNull(provider);
		this.configurer = configurer;
	}

	public DomainObjectIdentifier getIdentifier() {
		return identifier;
	}

	public Class<T> getType() {
		return identifier.getType();
	}

	public void configure(Action<? super T> action) {
		configurer.configure(identifier, action);
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return provider.map(transformer);
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return provider.flatMap(transformer);
	}
}
