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
package dev.nokee.xcode.project;

import dev.nokee.xcode.objects.PBXObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

interface PBXObjectCoder<T extends PBXObject> {
	Class<T> getType();

	T read(Decoder decoder);

	void write(Encoder encoder, T value);

	interface Encoder {
		void encodeArray(String key, Iterable<?> value);
		void encodeString(String key, CharSequence value);
		void encodeObject(String key, PBXObject value);
		void encodeObjects(String key, Iterable<? extends PBXObject> value);
		void encodeMap(String key, Map<String, ?> value);

		default void encodeInteger(String key, int value) {
			encodeString(key, String.valueOf(value));
		}
	}

	interface Decoder {
		<S extends PBXObject> Optional<S> decodeObject(String key);
		<S extends PBXObject> void decodeObjectsIfPresent(String key, Consumer<? super Iterable<S>> consumer);

		void decodeMapIfPresent(String key, Consumer<? super Map<String, Object>> consumer);

		void decodeArrayIfPresent(String key, Consumer<? super Iterable<?>> consumer);

		<S> void decodeIfPresent(String key, Consumer<? super S> action);

		default <S> void decodeIfPresent(String key, Class<S> type, Consumer<? super S> action) {
			decodeIfPresent(key,(Type) type, action);
		}

		<S> void decodeIfPresent(String key, Type type, Consumer<? super S> action);
	}
}
