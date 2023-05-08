/*
 * Copyright 2023 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
abstract class AbstractCodeable implements Codeable, Serializable {
	private KeyedObject delegate;

	protected AbstractCodeable(KeyedObject delegate) {
		this.delegate = delegate;
	}

	protected final KeyedObject delegate() {
		return delegate;
	}

	@Override
	public final String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public final String globalId() {
		return delegate.globalId();
	}

	@Override
	public final <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	@Override
	public Map<CodingKey, Object> getAsMap() {
		return delegate.getAsMap();
	}

	protected final <T> Optional<T> getAsOptional(CodingKey key) {
		return Optional.ofNullable(tryDecode(key));
	}

	protected final <T> List<T> getOrEmptyList(CodingKey key) {
		final List<T> value = delegate.tryDecode(key);
		if (value == null) {
			return ImmutableList.of();
		} else {
			return value;
		}
	}

	protected final <K> Map<K, ?> getOrEmptyMap(CodingKey key) {
		final Map<K, ?> value = delegate.tryDecode(key);
		if (value == null) {
			return ImmutableMap.of();
		} else {
			return value;
		}
	}

	protected final boolean getOrFalse(CodingKey key) {
		final Boolean value = delegate.tryDecode(key);
		if (value == null) {
			return false;
		} else {
			return value;
		}
	}

	@Nullable
	protected final <T> T getOrNull(CodingKey key) {
		return tryDecode(key);
	}

	@Override
	public final long age() {
		return delegate.age();
	}

	@Override
	public final void encode(EncodeContext context) {
		delegate.encode(context);
	}

	private void writeObject(ObjectOutputStream outStream) throws IOException {
		val data = getAsMap().entrySet().stream()
			// TODO: all coding key should be serializable and the filtering should be done during encoding
			.filter(it -> it.getKey() instanceof Enum || it.getKey() instanceof SerializableCodingKey)
			.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
		outStream.writeObject(data);
	}

	private void readObject(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
		val builder = DefaultKeyedObject.builder();
		@SuppressWarnings("unchecked")
		val codingKeyToValues = (Map<CodingKey, Object>) inStream.readObject();
		codingKeyToValues.forEach(builder::put);
		delegate = builder.build();
	}
}
