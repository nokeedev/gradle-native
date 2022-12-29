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
import dev.nokee.xcode.project.ValueDecoder;

public final class StringDecoder<OUT> implements ValueDecoder<OUT, Object> {
	private final ValueDecoder<OUT, String> delegate;

	public StringDecoder(ValueDecoder<OUT, String> delegate) {
		this.delegate = delegate;
	}

	@Override
	public OUT decode(Object object, Context context) {
		return Select.newIfElseCases().forCase(it -> it instanceof String, o -> delegate.decode((String) o, context)).select(object);
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

	public static StringDecoder<String> newStringDecoder() {
		return new StringDecoder<>(new NoOpDecoder<>(CoderType.string()));
	}
}
