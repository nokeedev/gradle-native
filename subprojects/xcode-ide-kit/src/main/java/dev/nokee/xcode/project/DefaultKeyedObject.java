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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode
public final class DefaultKeyedObject implements KeyedObject {
	private final KeyedObject parent;
	private final ImmutableMap<CodingKey, Object> values;

	public DefaultKeyedObject(ImmutableMap<CodingKey, Object> values) {
		this(null, values);
	}

	public DefaultKeyedObject(KeyedObject parent, ImmutableMap<CodingKey, Object> values) {
		this.parent = parent;
		this.values = values;
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

	public static final class Builder {
		private KeyedObject parent = null;
		private final ImmutableMap.Builder<CodingKey, Object> builder = ImmutableMap.builder();
		private final Set<CodingKey> requiredKeys = new LinkedHashSet<>();
		private boolean lenient = false;

		public Builder put(CodingKey key, @Nullable Object object) {
			if (object != null) {
				builder.put(key, object);
			}
			return this;
		}

		public Builder parent(KeyedObject parent) {
			this.parent = parent;
			return this;
		}

		public DefaultKeyedObject build() {
			DefaultKeyedObject result = new DefaultKeyedObject(parent, builder.build());
			if (!lenient) {
				for (CodingKey it : requiredKeys) {
					if (!result.has(it)) {
						throw new NullPointerException(String.format("'%s' must not be null", it));
					}
				}
			}
			return result;
		}

		public Builder requires(CodingKey... codingKey) {
			requiredKeys.addAll(Arrays.asList(codingKey));
			return this;
		}

		public Builder lenient() {
			lenient = true;
			return this;
		}
    }

	@Override
	public String toString() {
		return String.format("%s gid=none", Optional.ofNullable(tryDecode(KeyedCoders.ISA)).orElse("object"));
	}
}
