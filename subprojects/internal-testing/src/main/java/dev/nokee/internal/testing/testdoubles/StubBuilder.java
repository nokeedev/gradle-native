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
import dev.nokee.internal.testing.reflect.ReturnInformation;

public interface StubBuilder<THIS, R extends ReturnInformation> {
	OngoingStubbing<THIS, R> then(Answer<THIS, R> answer);

	<V> OngoingStubbing<THIS, R> capture(Captor<V> captor);

	interface WithArguments<THIS, R extends ReturnInformation, A extends ArgumentInformation> {

		StubBuilder<THIS, R> with(StubArguments<A> argStub);

		static <A0> StubArguments<ArgumentInformation.Arg1<A0>> args(A0 a0) {
			return new StubArguments<ArgumentInformation.Arg1<A0>>() {
				private final Object[] args = new Object[] {a0};

				@SuppressWarnings("unchecked")
				@Override
				public <A> A getArgument(int index) {
					return (A) args[index];
				}
			};
		}

		static <A0, A1> StubArguments<ArgumentInformation.Arg2<A0, A1>> args(A0 a0, A1 a1) {
			return new StubArguments<ArgumentInformation.Arg2<A0, A1>>() {
				private final Object[] args = new Object[] { a0, a1 };

				@SuppressWarnings("unchecked")
				@Override
				public <A> A getArgument(int index) {
					return (A) args[index];
				}
			};
		}

		static <A0, A1, A2> StubArguments<ArgumentInformation.Arg3<A0, A1, A2>> args(A0 a0, A1 a1, A2 a2) {
			return new StubArguments<ArgumentInformation.Arg3<A0, A1, A2>>() {
				private final Object[] args = new Object[] { a0, a1, a2 };

				@SuppressWarnings("unchecked")
				@Override
				public <A> A getArgument(int index) {
					return (A) args[index];
				}
			};
		}
	}
}
