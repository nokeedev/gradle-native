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

import java.util.List;
import java.util.Map;

public abstract class KeyedEncoderAdapter implements KeyedEncoder {
	protected abstract void tryEncode(String key, Object value);

	private Encoder newEncoder(String key) {
		return new EncoderAdapter() {
			@Override
			protected String encodeRef(Codeable object) {
				return KeyedEncoderAdapter.this.encodeRef(object);
			}

			@Override
			protected Map<String, ?> encodeObj(Codeable object) {
				return KeyedEncoderAdapter.this.encodeObj(object);
			}

			@Override
			protected void tryEncode(Object value) {
				KeyedEncoderAdapter.this.tryEncode(key, value);
			}
		};
	}

	@Override
	public <T> void encode(String key, T value, ValueCoder<T> decoder) {
		decoder.encode(value, newEncoder(key));
	}

	@Override
	public void encodeByRefObject(String key, Object object) {
		newEncoder(key).encodeByRefObject(object);
	}

	@Override
	public void encodeByCopyObject(String key, Object object) {
		newEncoder(key).encodeByCopyObject(object);
	}

	protected abstract String encodeRef(Codeable object);

	protected abstract Map<String, ?> encodeObj(Codeable object);

	@Override
	public void encodeString(String key, CharSequence string) {
		newEncoder(key).encodeString(string);
	}

	@Override
	public void encodeInteger(String key, int integer) {
		newEncoder(key).encodeInteger(integer);
	}

	@Override
	public void encodeDictionary(String key, Map<String, ?> dict) {
		newEncoder(key).encodeDictionary(dict);
	}

	@Override
	public void encodeBoolean(String key, boolean value) {
		newEncoder(key).encodeBoolean(value);
	}

	@Override
	public <T> void encodeArray(String key, List<T> values, ValueEncoder<T, Object> encoder) {
		newEncoder(key).encodeArray(values, encoder);
	}
}
