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
import java.util.Optional;

@EqualsAndHashCode
public final class DefaultKeyedObject implements KeyedObject {
	private final ImmutableMap<CodingKey, Object> values;

	public DefaultKeyedObject(ImmutableMap<CodingKey, Object> values) {
		this.values = values;
	}

	@Nullable
	@Override
	public String globalId() {
		return null;
	}

	@Override
	public String isa() {
		return tryDecode(KeyedCoders.ISA);
	}

	@Override
	public void encode(EncodeContext context) {
		context.tryEncode(values);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		@SuppressWarnings("unchecked")
		final T result = (T) values.get(key);
		return result;
	}

	public static final class Builder {
		private final ImmutableMap.Builder<CodingKey, Object> builder = ImmutableMap.builder();

		public Builder put(CodingKey key, @Nullable Object object) {
			if (object != null) {
				builder.put(key, object);
			}
			return this;
		}

		public DefaultKeyedObject build() {
			return new DefaultKeyedObject(builder.build());
		}
	}

	@Override
	public String toString() {
		return String.format("%s gid=none", Optional.ofNullable(tryDecode(KeyedCoders.ISA)).orElse("object"));
	}
}
