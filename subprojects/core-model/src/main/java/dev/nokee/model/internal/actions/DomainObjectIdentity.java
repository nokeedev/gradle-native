/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represent what an entity is all about in terms of projection actions
 */
final class DomainObjectIdentity {
	private static final DomainObjectIdentity EMPTY = new DomainObjectIdentity(ImmutableSetMultimap.of());
	private final SetMultimap<Class<?>, Object> values;

	private DomainObjectIdentity(SetMultimap<Class<?>, Object> values) {
		this.values = ImmutableSetMultimap.copyOf(values);
	}

	public static DomainObjectIdentity of(Object value) {
		return new DomainObjectIdentity(ImmutableSetMultimap.of(value.getClass(), value));
	}

	public static DomainObjectIdentity of(Iterable<?> value) {
		if (Iterables.isEmpty(value)) {
			return EMPTY;
		}
		val firstElement = Iterables.getFirst(value, null);
		return new DomainObjectIdentity(ImmutableSetMultimap.<Class<?>, Object>builder().putAll(firstElement.getClass(), value).build());
	}

	public <T> Optional<T> get(Class<T> type) {
		Objects.requireNonNull(type);
		@SuppressWarnings("unchecked")
		T value = (T) Iterables.getOnlyElement(values.get(type), null);
		return Optional.ofNullable(value);
	}

	public <T> Set<T> getAll(Class<T> type) {
		Objects.requireNonNull(type);
		@SuppressWarnings("unchecked")
		Set<T> value = (Set<T>) values.get(type);
		return value;
	}

	public <T> DomainObjectIdentity with(T value) {
		Objects.requireNonNull(value);
		if (value instanceof Iterable) {
			return with((Iterable<? extends Object>) value);
		} else {
			val result = MultimapBuilder.hashKeys().hashSetValues().build(values);
			result.replaceValues(value.getClass(), ImmutableSet.of(value));
			return new DomainObjectIdentity(result);
		}
	}

	public <T> DomainObjectIdentity with(Iterable<T> value) {
		Objects.requireNonNull(value);
		if (Iterables.isEmpty(value)) {
			return this;
		}
		val firstElement = Iterables.getFirst(value, null);
		val result = MultimapBuilder.hashKeys().hashSetValues().build(values);
		result.replaceValues(firstElement.getClass(), value);
		return new DomainObjectIdentity(result);
	}

	public <T> DomainObjectIdentity plus(T value) {
		Objects.requireNonNull(value);
		val result = MultimapBuilder.hashKeys().hashSetValues().build(values);
		result.put(value.getClass(), value);
		return new DomainObjectIdentity(result);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
