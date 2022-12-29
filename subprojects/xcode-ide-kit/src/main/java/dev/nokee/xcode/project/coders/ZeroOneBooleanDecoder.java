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

public final class ZeroOneBooleanDecoder implements ValueDecoder<Boolean, Object> {
	private final ValueDecoder<Boolean, Object> delegate;

	public ZeroOneBooleanDecoder() {
		this(new ThrowingValueDecoder<>(CoderType.oneZeroBoolean()));
	}

	public ZeroOneBooleanDecoder(ValueDecoder<Boolean, Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Boolean decode(Object object, Context context) {
		if (object instanceof String) {
			try {
				int value = Integer.parseInt((String) object);
				if (value == 0) {
					return false;
				} else if (value == 1) {
					return true;
				}
			} catch (NumberFormatException ex) {
				// ignore format issue...
			}
		} else if (object instanceof Integer) {
			int value = (Integer) object;
			if (value == 0) {
				return false;
			} else if (value == 1) {
				return true;
			}
		}
		return delegate.decode(object, context);
	}

	@Override
	public CoderType<?> getDecodeType() {
		return delegate.getDecodeType();
	}
}
