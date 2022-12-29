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

import com.google.common.base.Preconditions;
import dev.nokee.xcode.project.Decoder;
import dev.nokee.xcode.project.Encoder;
import dev.nokee.xcode.project.ValueCoder;
import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.project.ValueEncoder;

public final class DefaultCoder<OUT> implements ValueCoder<OUT> {
	private final ValueDecoder<OUT, Object> decoder;
	private final ValueEncoder<Object, OUT> encoder;

	public DefaultCoder(ValueDecoder<OUT, Object> decoder, ValueEncoder<Object, OUT> encoder) {
		Preconditions.checkArgument(decoder.getDecodeType().equals(encoder.getEncodeType()));
		this.decoder = decoder;
		this.encoder = encoder;
	}

	@Override
	public OUT decode(Decoder context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void encode(OUT value, Encoder context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OUT decode(Object object, ValueDecoder.Context context) {
		return decoder.decode(object, context);
	}

	@Override
	public CoderType<?> getDecodeType() {
		return decoder.getDecodeType();
	}

	@Override
	public void accept(ValueDecoder.Visitor visitor) {
		ValueCoder.super.accept(visitor);
		decoder.accept(visitor);
	}

	@Override
	public Object encode(OUT value, ValueEncoder.Context context) {
		return encoder.encode(value, context);
	}

	@Override
	public CoderType<?> getEncodeType() {
		return encoder.getEncodeType();
	}

	@Override
	public void accept(ValueEncoder.Visitor visitor) {
		ValueCoder.super.accept(visitor);
		encoder.accept(visitor);
	}

	public ValueDecoder<OUT, Object> getDecoder() {
		return decoder;
	}

	public ValueEncoder<Object, OUT> getEncoder() {
		return encoder;
	}
}
