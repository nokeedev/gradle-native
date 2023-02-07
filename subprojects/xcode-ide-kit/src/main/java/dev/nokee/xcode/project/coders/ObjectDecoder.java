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

import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode
public final class ObjectDecoder<OUT> implements ValueDecoder<OUT, Object> {
	private final ValueDecoder<OUT, KeyedObject> delegate;

	public ObjectDecoder(ValueDecoder<OUT, KeyedObject> delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public OUT decode(Object object, Context context) {
		return Select.newIfElseCases().forCase(it -> it instanceof Map, o -> delegate.decode(context.decodeBycopyObject((Map<String, ?>) o), context)).select(object);
	}
}
