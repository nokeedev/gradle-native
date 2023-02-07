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

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class ListDecoder<OUT> implements ValueDecoder<List<OUT>, Object> {
	private final ValueDecoder<OUT, Object> delegate;

	public ListDecoder(ValueDecoder<OUT, Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<OUT> decode(Object object, Context context) {
		return Select.newIfElseCases().forCase(it -> it instanceof List, o -> ((List<?>) o).stream().map(it -> delegate.decode(it, context)).collect(Collectors.toList())).select(object);
	}
}
