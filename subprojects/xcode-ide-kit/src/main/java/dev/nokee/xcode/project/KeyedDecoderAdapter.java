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

import javax.annotation.Nullable;
import java.util.Map;

public abstract class KeyedDecoderAdapter implements KeyedDecoder, ValueDecoder.Context {
	private final Map<String, ?> values;

	public KeyedDecoderAdapter(Map<String, ?> values) {
		this.values = values;
	}

	@Nullable
	@Override
	public <T> T decode(String key, ValueDecoder<T, Object> decoder) {
		if (!values.containsKey(key)) {
			return null;
		}

		try {
			return decoder.decode(values.get(key), this);
		} catch (Throwable ex) {
			throw new RuntimeException(String.format("Exception while decoding '%s', value %s", key, values.get(key)), ex);
		}
	}
}
