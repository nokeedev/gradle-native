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

public abstract class EncoderAdapter implements Encoder {
	@Override
	public final void encodeByRefObject(Object object) {
		tryEncode(encodeRef((Codeable) object));
	}

	@Override
	public final void encodeByCopyObject(Object object) {
		tryEncode(encodeObj((Codeable) object));
	}

	protected abstract String encodeRef(Codeable object);

	protected abstract Map<String, ?> encodeObj(Codeable object);

	protected abstract void tryEncode(Object value);

	@Override
	public final void encodeString(CharSequence string) {
		tryEncode(string);
	}

	@Override
	public final void encodeInteger(int integer) {
		tryEncode(integer);
	}

	@Override
	public final void encodeDictionary(Map<String, ?> dict) {
		tryEncode(dict);
	}

	@Override
	public final void encodeBoolean(boolean value) {
		tryEncode(value);
	}

	@Override
	public final <T> void encodeArray(List<T> values, ValueCoder<T> encoder) {
		final ImmutableList.Builder<T> builder = ImmutableList.builder();
		values.stream().forEach(it -> encoder.encode(it, new EncoderAdapter() {
			@Override
			protected String encodeRef(Codeable object) {
				return EncoderAdapter.this.encodeRef(object);
			}

			@Override
			protected Map<String, ?> encodeObj(Codeable object) {
				return EncoderAdapter.this.encodeObj(object);
			}

			@Override
			protected void tryEncode(Object value) {
				@SuppressWarnings("unchecked")
				final T element = (T) value;
				builder.add(element);
			}
		}));
		tryEncode(builder.build());
	}
}
