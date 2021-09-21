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

import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface RealizableDomainObjectRepository<T> {
	Set<T> filter(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	Provider<Set<T>> filtered(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	<S extends T> S get(TypeAwareDomainObjectIdentifier<S> identifier);
	<S extends T> Provider<S> identified(TypeAwareDomainObjectIdentifier<S> identifier);

	boolean anyKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	Optional<TypeAwareDomainObjectIdentifier<? extends T>> findKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
}
