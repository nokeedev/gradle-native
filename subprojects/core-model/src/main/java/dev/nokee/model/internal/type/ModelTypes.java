/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model.internal.type;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModelTypes {
	/**
	 * Returns {@literal ModelType} for a {@literal Set} of the specified element type.
	 *
	 * @param elementType  the element type of the set, must not be null
	 * @param <E>  the element type
	 * @return a {@link ModelType} for a {@link Set} of the specified element type, never null
	 */
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <E> ModelType<Set<E>> set(ModelType<E> elementType) {
		return (ModelType<Set<E>>) ModelType.of(new TypeToken<Set<E>>() {}
			.where(new TypeParameter<E>() {}, (TypeToken<E>) TypeToken.of(elementType.getType()))
			.getType());
	}

	/**
	 * Returns {@literal ModelType} for a {@literal List} of the specified element type.
	 *
	 * @param elementType  the element type of the list, must not be null
	 * @param <E>  the element type
	 * @return a {@link ModelType} for a {@link List} of the specified element type, never null
	 */
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <E> ModelType<List<E>> list(ModelType<E> elementType) {
		return (ModelType<List<E>>) ModelType.of(new TypeToken<List<E>>() {}
			.where(new TypeParameter<E>() {}, (TypeToken<E>) TypeToken.of(elementType.getType()))
			.getType());
	}

	/**
	 * Returns {@literal ModelType} for a {@literal Map} of the specified key and value type.
	 *
	 * @param keyType  the key type of the map, must not be null
	 * @param valueType  the value type of the map, must not be null
	 * @param <K>  the key type
	 * @param <V>  the value type
	 * @return a {@link ModelType} for a {@link Map} of the specified key and value type, never null
	 */
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <K, V> ModelType<Map<K, V>> map(ModelType<K> keyType, ModelType<V> valueType) {
		return (ModelType<Map<K, V>>) ModelType.of(new TypeToken<Map<K, V>>() {}
			.where(new TypeParameter<K>() {}, (TypeToken<K>) TypeToken.of(keyType.getType()))
			.where(new TypeParameter<V>() {}, (TypeToken<V>) TypeToken.of(valueType.getType()))
			.getType());
	}
}
