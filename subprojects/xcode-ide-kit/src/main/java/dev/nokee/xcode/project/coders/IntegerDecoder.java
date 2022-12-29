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

import dev.nokee.xcode.project.ValueDecoder;

public final class IntegerDecoder<OUT> implements ValueDecoder<OUT, Object> {
	private final ValueDecoder<OUT, Integer> delegate;

	public IntegerDecoder(ValueDecoder<OUT, Integer> delegate) {
		this.delegate = delegate;
	}

	@Override
	public OUT decode(Object object, Context context) {
		if (object instanceof Integer) {
			return delegate.decode((Integer) object, context);
		} else if (object instanceof String) {
			try {
				return delegate.decode(Integer.parseInt((String) object), context);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("'object' must be integer convertible type", ex);
			}
		} else {
			throw new IllegalArgumentException("'object' must be integer convertible type");
		}
	}

	@Override
	public CoderType<?> getDecodeType() {
		return delegate.getDecodeType();
	}

	@Override
	public void accept(Visitor visitor) {
		ValueDecoder.super.accept(visitor);
		delegate.accept(visitor);
	}

	public static IntegerDecoder<Integer> newIntegerDecoder() {
		return new IntegerDecoder<>(new NoOpDecoder<>(CoderType.integer()));
	}
}
