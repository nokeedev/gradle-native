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

public final class NoYesBooleanDecoder implements ValueDecoder<Boolean, Object> {
	private final ValueDecoder<Boolean, Object> delegate;

	public NoYesBooleanDecoder() {
		this(new ThrowingValueDecoder<>(CoderType.yesNoBoolean()));
	}

	public NoYesBooleanDecoder(ValueDecoder<Boolean, Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Boolean decode(Object object, Context context) {
		if (object instanceof String) {
			final String value = (String) object;
			if ("YES".equals(value)) {
				return true;
			} else if ("NO".equals(value)) {
				return false;
			}
		}
		return delegate.decode(object, context);
	}

	@Override
	public CoderType<Boolean> getDecodeType() {
		return CoderType.or(CoderType.yesNoBoolean(), delegate.getDecodeType());
	}

	@Override
	public void accept(Visitor visitor) {
		ValueDecoder.super.accept(visitor);
		delegate.accept(visitor);
	}
}
