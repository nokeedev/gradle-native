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

import dev.nokee.xcode.project.ValueEncoder;

import java.util.List;
import java.util.stream.Collectors;

public final class ListEncoder<IN> implements ValueEncoder<Object, List<IN>> {
	private final ValueEncoder<? extends Object, IN> delegate;

	public ListEncoder(ValueEncoder<? extends Object, IN> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object encode(List<IN> value, Context context) {
		return value.stream().map(it -> delegate.encode(it, context)).collect(Collectors.toList());
	}

	@Override
	public CoderType<?> getEncodeType() {
		return CoderType.listOf(delegate.getEncodeType());
	}

	@Override
	public void accept(Visitor visitor) {
		ValueEncoder.super.accept(visitor);
		delegate.accept(visitor);
	}
}
