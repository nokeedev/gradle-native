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

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class DecoderAdapter implements Decoder {
	private final Object value;

	public DecoderAdapter(Object value) {
		this.value = value;
	}

	protected abstract KeyedObject create(String gid);

	protected abstract KeyedObject create(Map<String, ?> value);

	@Override
	public final <T extends Codeable> T decodeObject(CodeableObjectFactory<T> factory) {
		if (value instanceof String) {
			return factory.create(new CachingKeyedObject(create((String) value)));
		} else if (value instanceof Map) {
			return factory.create(new CachingKeyedObject(create((Map<String, ?>) value)));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final boolean decodeBoolean() {
		if (value instanceof Boolean) {
			return (boolean) value;
		} else if (value instanceof String) {
			switch ((String) value) {
				case "0":
				case "NO":
					return false;
				case "1":
				case "YES":
					return true;
				default:
					throw new UnsupportedOperationException();
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final Map<String, Object> decodeDictionary() {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> result = (Map<String, Object>) value;
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public String decodeString() {
		if (value instanceof String) {
			return (String) value;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final int decodeInteger() {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof String) {
			return Integer.parseInt((String) value);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final <T> List<T> decodeArray(ValueCoder<T> decoder) {
		if (value instanceof List) {
			final ImmutableList.Builder<T> builder = ImmutableList.builder();
			for (Object it : ((List<?>) value)) {
				builder.add(decoder.decode(new DecoderAdapter(it) {
					@Override
					protected KeyedObject create(String gid) {
						return DecoderAdapter.this.create(gid);
					}

					@Override
					protected KeyedObject create(Map<String, ?> value) {
						return DecoderAdapter.this.create(value);
					}
				}));
			}
			return builder.build();
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
