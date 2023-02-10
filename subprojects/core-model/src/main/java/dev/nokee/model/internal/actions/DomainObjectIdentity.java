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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import lombok.val;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represent what an entity is all about in terms of projection actions
 */
final class DomainObjectIdentity {
	private static final DomainObjectIdentity EMPTY = new DomainObjectIdentity(ImmutableMap.of());
	private final Map<Class<?>, Object> values;

	private DomainObjectIdentity(Map<Class<?>, Object> values) {
		this.values = values;
	}

	public static DomainObjectIdentity of(Object value) {
		if (value instanceof Iterable) {
			return of((Iterable<?>) value);
		}
		return new DomainObjectIdentity(ImmutableMap.of(value.getClass(), value));
	}

	public static DomainObjectIdentity of(Iterable<?> value) {
		if (Iterables.isEmpty(value)) {
			return EMPTY;
		}
		val firstElement = Iterables.getFirst(value, null);
		return new DomainObjectIdentity(ImmutableMap.<Class<?>, Object>builder().put(firstElement.getClass(), value).build());
	}

	@Nullable
	public <T> T get(Class<T> type) {
		Objects.requireNonNull(type);
		final Object value = values.get(type);
		if (value == null) {
			return null;
		} else if (!(value instanceof Iterable)) {
			@SuppressWarnings("unchecked") final T result = (T) value;
			return result;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public <T> Set<T> getAll(Class<T> type) {
		Objects.requireNonNull(type);
		@SuppressWarnings("unchecked")
		Set<T> value = (Set<T>) values.getOrDefault(type, ImmutableSet.of());
		return value;
	}

	public <T> DomainObjectIdentity with(T value) {
		Objects.requireNonNull(value);
		if (value instanceof Iterable) {
			return with((Iterable<? extends Object>) value);
		} else {
			val result = new HashMap<Class<?>, Object>(values);
			result.put(value.getClass(), value);
			return new DomainObjectIdentity(result);
		}
	}

	public <T> DomainObjectIdentity with(Iterable<T> value) {
		Objects.requireNonNull(value);
		if (Iterables.isEmpty(value)) {
			return this;
		}
		val firstElement = Iterables.getFirst(value, null);
		val result = new HashMap<Class<?>, Object>(values);
		result.put(firstElement.getClass(), value);
		return new DomainObjectIdentity(result);
	}

	public <T> DomainObjectIdentity plus(T value) {
		Objects.requireNonNull(value);
		val result = new HashMap<Class<?>, Object>(values);
		result.compute(value.getClass(), (k, v) -> {
			if (v == null) {
				return ImmutableSet.of(value);
			} else if (v instanceof Iterable) {
				return ImmutableSet.<Object>builder().addAll((Iterable<?>) v).add(value).build();
			} else {
				return ImmutableSet.<Object>builder().add(v).add(value).build();
			}
		});
		return new DomainObjectIdentity(result);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
