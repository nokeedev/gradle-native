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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MoreCollectors;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class DefaultKeyedObject implements KeyedObject {
	@EqualsAndHashCode.Exclude private final long age = System.nanoTime(); // signal this object is newer than parent
	private final KeyedObject parent;
	private final ImmutableMap<CodingKey, Object> values;
	private final Set<CodingKey> knownKeys;

	public DefaultKeyedObject(ImmutableMap<CodingKey, Object> values) {
		this(null, values, values.keySet());
	}

	public DefaultKeyedObject(KeyedObject parent, ImmutableMap<CodingKey, Object> values, Set<CodingKey> knownKeys) {
		this.parent = parent;
		this.values = values;
		this.knownKeys = knownKeys;
	}

	@Nullable
	@Override
	public String globalId() {
		if (parent == null) {
			return null;
		} else {
			return parent.globalId();
		}
	}

	@Override
	public Map<CodingKey, Object> getAsMap() {
		if (parent == null) {
			return values;
		} else {
			val result = new LinkedHashMap<CodingKey, Object>();
			result.putAll(parent.getAsMap().entrySet().stream().map(it -> {
				val key = it.getKey();
				val knownKey = knownKeys.stream().filter(t -> key.getName().equals(t.getName())).collect(MoreCollectors.toOptional());
				if (knownKey.isPresent()) {
					return new AbstractMap.SimpleImmutableEntry<>(knownKey.get(), tryDecode(knownKey.get()));
				} else {
					return it;
				}
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			result.putAll(values);
			return result;
		}
	}

	@Override
	public long age() {
		return age;
	}

	@Override
	public String isa() {
		return tryDecode(KeyedCoders.ISA);
	}

	@Override
	public void encode(EncodeContext context) {
		if (parent != null) {
			parent.encode(context);
		}
		context.tryEncode(values);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		@SuppressWarnings("unchecked")
		T result = (T) values.get(key);
		if (result == null && parent != null) {
			result = parent.tryDecode(key);
		}
		return result;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private KeyedObject parent = null;
		private final LinkedHashMap<CodingKey, Object> builder = new LinkedHashMap<>();
		private final LinkedHashMap<CodingKey, Object> defaultValues = new LinkedHashMap<>();
		private final List<Predicate<? super KeyedObject>> requirements = new ArrayList<>();
		private boolean lenient = false;
		private final Set<CodingKey> knownKeys = new HashSet<>();

		public Builder knownKeys(CodingKey... keys) {
			knownKeys.addAll(Arrays.asList(keys));
			return this;
		}

		public Builder put(CodingKey key, Object object) {
			Objects.requireNonNull(object, () -> String.format("'%s' must not be null", key.getName()));
			builder.put(key, object);
			return this;
		}

		public Builder put(CodingKey key, Iterable<?> object) {
			Objects.requireNonNull(object, () -> String.format("'%s' must not be null", key.getName()));
			builder.put(key, ImmutableList.copyOf(object));
			return this;
		}

		public Builder putNullable(CodingKey key, @Nullable Object object) {
			if (object != null) {
				builder.put(key, object);
			}
			return this;
		}

		public Builder parent(KeyedObject parent) {
			this.parent = parent;
			return this;
		}

		public boolean hasParent() {
			return parent != null;
		}

		public DefaultKeyedObject build() {
			defaultValues.forEach((k, v) -> {
				if (!(builder.containsKey(k) || (parent != null && parent.has(k)))) {
					builder.put(k, v);
				}
			});
			DefaultKeyedObject result = new DefaultKeyedObject(parent, ImmutableMap.copyOf(builder), knownKeys);
			if (!lenient) {
				for (Predicate<? super KeyedObject> requirement : requirements) {
					if (!requirement.test(result)) {
						throw new NullPointerException(String.format("%s must not be null", requirement));
					}
				}
			}
			return result;
		}

		public Builder lenient() {
			lenient = true;
			return this;
		}

		public Builder requires(Predicate<? super KeyedObject> predicate) {
			requirements.add(predicate);
			return this;
		}

		public void add(CodingKey codingKey, Object element) {
			Objects.requireNonNull(element, () -> String.format("'%s' must not be null", codingKey.getName()));
			Object value = builder.get(codingKey);
			if (value == null) {
				builder.put(codingKey, ImmutableList.of(element));
			} else if (value instanceof List) {
				builder.put(codingKey, ImmutableList.builder().addAll((List<?>) value).add(element).build());
			} else {
				throw new UnsupportedOperationException();
			}
		}

		public Builder ifAbsent(CodingKey codingKey, Object value) {
			defaultValues.put(codingKey, value);
			return this;
		}
	}

	@Override
	public String toString() {
		return String.format("%s gid=none", Optional.ofNullable(tryDecode(KeyedCoders.ISA)).orElse("object"));
	}

	public static Predicate<KeyedObject> key(CodingKey codingKey) {
		return new Predicate<KeyedObject>() {
			@Override
			public boolean test(KeyedObject it) {
				return it.has(codingKey);
			}

			@Override
			public Predicate<KeyedObject> or(Predicate<? super KeyedObject> other) {
				final Predicate<KeyedObject> firstPredicate = this;
				return new Predicate<KeyedObject>() {
					@Override
					public boolean test(KeyedObject keyedObject) {
						return firstPredicate.test(keyedObject) || other.test(keyedObject);
					}

					@Override
					public String toString() {
						return "'" + codingKey + "' or " + other;
					}
				};
			}

			@Override
			public String toString() {
				return "'" + codingKey.getName() + "'";
			}
		};
	}
}
