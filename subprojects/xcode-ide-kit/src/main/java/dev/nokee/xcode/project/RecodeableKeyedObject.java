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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;


@EqualsAndHashCode
public final class RecodeableKeyedObject implements KeyedObject {
	private final KeyedObject delegate;
	private final KnownCodingKeys knownKeys;

	public RecodeableKeyedObject(KeyedObject delegate, KnownCodingKeys knownKeys) {
		this.delegate = delegate;
		this.knownKeys = knownKeys;
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public Map<CodingKey, Object> getAsMap() {
		return delegate.getAsMap().entrySet().stream().map(it -> {
			val knownKey = knownKeys.knows(it.getKey());
			if (knownKey.isPresent()) {
				return new AbstractMap.SimpleImmutableEntry<>(knownKey.get(), tryDecode(knownKey.get()));
			} else {
				return it;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public long age() {
		return delegate.age();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
		context.tryEncode(stream(knownKeys).flatMap(key -> {
			final Object value = delegate.tryDecode(key);
			if (value != null) {
				return Stream.of(new AbstractMap.SimpleImmutableEntry<>(key, value));
			} else {
				return Stream.empty();
			}
		}).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public interface KnownCodingKeys extends Iterable<CodingKey> {
		Optional<CodingKey> knows(CodingKey key);
	}

	public static KnownCodingKeys ofIsaAnd(CodingKey[] values) {
		return new DefaultKnownCodingKeys(ImmutableSet.<CodingKey>builder().add(KeyedCoders.ISA).add(values).build());
	}

	public static KnownCodingKeys of(CodingKey[] values) {
		return new DefaultKnownCodingKeys(ImmutableSet.copyOf(values));
	}

	@EqualsAndHashCode
	private static final class DefaultKnownCodingKeys implements KnownCodingKeys {
		private final Set<CodingKey> knownKeys;

		private DefaultKnownCodingKeys(Set<CodingKey> knownKeys) {
			this.knownKeys = knownKeys;
		}

		public Optional<CodingKey> knows(CodingKey key) {
			return knownKeys.stream().filter(it -> key.getName().equals(it.getName())).collect(MoreCollectors.toOptional());
		}

		@Override
		public Iterator<CodingKey> iterator() {
			return knownKeys.iterator();
		}
	}
}
