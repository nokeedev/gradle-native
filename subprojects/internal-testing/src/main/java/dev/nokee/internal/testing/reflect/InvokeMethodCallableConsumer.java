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

import java.util.function.Consumer;

final class InvokeMethodCallableConsumer<ReceiverType> implements Consumer<ReceiverType> {
	private final MethodCallable<ReceiverType, ?, ?> callable;

	InvokeMethodCallableConsumer(MethodCallable<ReceiverType, ?, ?> callable) {
		this.callable = callable;
	}

	@Override
	public void accept(ReceiverType it) {
		try {
			callable.invoke(it, new AlwaysNullArguments());
		} catch (
			Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static final class AlwaysNullArguments implements MethodCallable.Arguments {
		@Override
		public <A> A getArgument(int index) {
			return null;
		}
	}
}
