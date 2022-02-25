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
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represent what an entity is all about in terms of projection actions
 */
final class DomainObjectIdentity {
	private final Map<Class<?>, Object> values;

	private DomainObjectIdentity(Map<Class<?>, Object> values) {
		this.values = values;
	}

	public static DomainObjectIdentity of(Object value) {
		return new DomainObjectIdentity(ImmutableMap.of(value.getClass(), value));
	}

	public <T> Optional<T> get(Class<T> type) {
		Objects.requireNonNull(type);
		@SuppressWarnings("unchecked")
		T value = (T) values.get(type);
		return Optional.ofNullable(value);
	}

	public <T> DomainObjectIdentity with(T value) {
		Objects.requireNonNull(value);
		val result = new HashMap<>(values);
		result.put(value.getClass(), value);
		return new DomainObjectIdentity(ImmutableMap.copyOf(result));
	}
}
