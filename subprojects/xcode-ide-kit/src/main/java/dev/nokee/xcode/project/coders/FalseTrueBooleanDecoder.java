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
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class FalseTrueBooleanDecoder implements ValueDecoder<Boolean, Object> {
	private final ValueDecoder<Boolean, Object> delegate;

	public FalseTrueBooleanDecoder() {
		this(new ThrowingValueDecoder<>(CoderType.trueFalseBoolean()));
	}

	public FalseTrueBooleanDecoder(ValueDecoder<Boolean, Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Boolean decode(Object object, Context context) {
		if (object instanceof String) {
			final String value = (String) object;
			if ("false".equals(value)) {
				return false;
			} else if ("true".equals(value)) {
				return true;
			}
		} else if (object instanceof Boolean) {
			return (Boolean) object;
		}
		return delegate.decode(object, context);
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
}
