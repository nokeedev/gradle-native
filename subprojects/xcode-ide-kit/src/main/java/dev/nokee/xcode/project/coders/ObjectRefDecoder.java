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
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ObjectRefDecoder<OUT> implements ValueDecoder<OUT, Object> {
	private final ValueDecoder<OUT, KeyedObject> delegate;

	public ObjectRefDecoder(ValueDecoder<OUT, KeyedObject> delegate) {
		this.delegate = delegate;
	}

	@Override
	public OUT decode(Object object, Context context) {
		Preconditions.checkArgument(object instanceof String, "'object' must be a String");
		return delegate.decode(context.decodeByrefObject((String) object), context);
	}
}
