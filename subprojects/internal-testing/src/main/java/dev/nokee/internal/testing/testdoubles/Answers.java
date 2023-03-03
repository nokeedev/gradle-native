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

import dev.nokee.internal.testing.reflect.Invokable;
import dev.nokee.internal.testing.reflect.ReturnInformation;

import java.util.function.Supplier;

public final class Answers {
	@SafeVarargs
	public static <ReceiverType, ReturnType> Answer<ReceiverType, ReturnInformation.Return<ReturnType>> doReturn(ReturnType... values) {
		return new Answer<ReceiverType, ReturnInformation.Return<ReturnType>>() {
			private int i = 0;

			@Override
			public ReturnType answer(InvocationOnTestDouble<ReceiverType> invocation) throws Throwable {
				if (i >= values.length) {
					return null;
				} else {
					return values[i++];
				}
			}
		};
	}

	@SafeVarargs
	public static <ReceiverType, ReturnType> Answer<ReceiverType, ReturnInformation.Return<ReturnType>> doReturn(Supplier<ReturnType>... values) {
		return new Answer<ReceiverType, ReturnInformation.Return<ReturnType>>() {
			private int i = 0;

			@Override
			public ReturnType answer(InvocationOnTestDouble<ReceiverType> invocation) throws Throwable {
				if (i >= values.length) {
					return null;
				} else {
					return values[i++].get();
				}
			}
		};
	}

	public static <ReceiverType, ReturnType> Answer<ReceiverType, ReturnInformation.Return<ReturnType>> doReturn(Invokable<ReceiverType, ReturnType, ?> function) {
		return new Answer<ReceiverType, ReturnInformation.Return<ReturnType>>() {
			@Override
			public ReturnType answer(InvocationOnTestDouble<ReceiverType> invocation) throws Throwable {
				return function.invoke(invocation.getTestDouble(), invocation.getArguments());
			}
		};
	}

	public static <ReceiverType, ReturnType> Answer<ReceiverType, ReturnInformation.Return<ReturnType>> doAlwaysReturnNull() {
		return invocation -> null;
	}

	public static <ReceiverType, ReturnInfoType extends ReturnInformation> Answer<ReceiverType, ReturnInfoType> doThrow(Throwable... throwables) {
		return new Answer<ReceiverType, ReturnInfoType>() {
			private int i = 0;

			@Override
			public Object answer(InvocationOnTestDouble<ReceiverType> invocation) throws Throwable {
				if (i >= throwables.length) {
					return null;
				} else {
					throw throwables[i++];
				}
			}
		};
	}

	public static <ReceiverType, ReturnInfoType extends ReturnInformation> Answer<ReceiverType, ReturnInfoType> doNothing() {
		return new Answer<ReceiverType, ReturnInfoType>() {
			@Override
			public Object answer(InvocationOnTestDouble<ReceiverType> invocation) throws Throwable {
				// do nothing...
				return null;
			}
		};
	}
}
