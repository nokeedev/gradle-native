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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.KeyedDecoder;
import dev.nokee.xcode.project.KeyedEncoder;
import dev.nokee.xcode.project.ValueCoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class FieldCoder<T> implements KeyedCoder<T> {
	private final String key;
	private final ValueCoder<T> decoder;

	public FieldCoder(String key, ValueCoder<T> decoder) {
		this.key = key;
		this.decoder = decoder;
	}

	@Override
	public T decode(KeyedDecoder decoder) {
		return decoder.decode(key, this.decoder);
	}

	@Override
	public void encode(T value, KeyedEncoder encoder) {
		encoder.encode(key, value, decoder);
	}
}
