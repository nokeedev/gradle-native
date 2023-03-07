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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.project.ValueCoder;
import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class EncoderOnlyCoder<T> implements ValueCoder<T> {
	private final ValueEncoder<?, T> encoder;

	EncoderOnlyCoder(ValueEncoder<?, T> encoder) {
		this.encoder = encoder;
	}

	@Override
	public T decode(Object object, ValueDecoder.Context context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object encode(T value, ValueEncoder.Context context) {
		return encoder.encode(value, context);
	}
}
