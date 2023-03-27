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

import dev.nokee.util.internal.NotPredicate;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class IgnoreEmptyStringEncoder<OUT> implements ValueEncoder<OUT, List<String>> {
	private final ValueEncoder<OUT, List<String>> delegate;

	public IgnoreEmptyStringEncoder(ValueEncoder<OUT, List<String>> delegate) {
		this.delegate = delegate;
	}

	@Override
	public OUT encode(List<String> value, Context context) {
		return delegate.encode(value.stream().filter(new NotPredicate<>(String::isEmpty)).collect(Collectors.toList()), context);
	}
}
