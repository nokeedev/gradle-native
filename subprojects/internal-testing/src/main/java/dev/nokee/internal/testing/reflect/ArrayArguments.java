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
package dev.nokee.internal.testing.reflect;

import java.util.Arrays;
import java.util.stream.Collectors;

final class ArrayArguments implements Invokable.Arguments {
	private final Object[] args;

	public ArrayArguments(Object[] args) {
		this.args = args;
	}

	@Override
	public <A> A getArgument(int index) {
		@SuppressWarnings("unchecked")
		final A result = (A) args[index];
		return result;
	}

	@Override
	public String toString() {
		return "[" + Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")) + "]";
	}
}
