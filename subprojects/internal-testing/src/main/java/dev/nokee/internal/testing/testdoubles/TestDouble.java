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
package dev.nokee.internal.testing.testdoubles;

import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.internal.testing.reflect.MethodInformation;
import dev.nokee.internal.testing.reflect.ReturnInformation;

import java.util.function.Consumer;

public interface TestDouble<T> {
	static <THIS, R extends ReturnInformation> StubBuilder<THIS, R> callTo(MethodInformation<THIS, R> method) {
		return new OngoingStubBuilder<>(method);
	}

	static <THIS, ReturnInfoType extends ReturnInformation, ArgumentInfoType extends ArgumentInformation> StubBuilder.WithArguments<THIS, ReturnInfoType, ArgumentInfoType> callTo(MethodInformation.WithArguments<THIS, ReturnInfoType, ArgumentInfoType> method) {
		return new OngoingStubBuilder<>(method);
	}

	TestDouble<T> when(StubCall<T> result);

	TestDouble<T> alwaysThrows();

	default TestDouble<T> executeWith(Consumer<? super T> action) {
		action.accept(instance());
		return this;
	}

	MethodVerifier<ArgumentInformation.None> to(MethodInformation<T, ?> method);
	<A extends ArgumentInformation> MethodVerifier<A> to(MethodInformation.WithArguments<T, ?, A> method);

	T instance();

	default <R extends T> R instance(Narrower<R> narrower) {
		return narrower.narrow(instance());
	}

	interface Narrower<R> {
		R narrow(Object instance);
	}

	static <R> Narrower<R> narrowed() {
		return new Narrower<R>() {
			@Override
			@SuppressWarnings("unchecked")
			public R narrow(Object instance) {
				return (R) instance;
			}
		};
	}
}
