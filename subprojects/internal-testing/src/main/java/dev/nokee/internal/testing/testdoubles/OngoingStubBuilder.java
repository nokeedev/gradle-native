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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class OngoingStubBuilder<ReceiverType, ReturnInfoType extends ReturnInformation, ArgumentInfoType extends ArgumentInformation> implements OngoingStubbing<ReceiverType, ReturnInfoType>, StubBuilder.WithArguments<ReceiverType, ReturnInfoType, ArgumentInfoType> {
	private final List<Answer<ReceiverType, ReturnInfoType>> answers = new ArrayList<>();
	private final MethodInformation<ReceiverType, ReturnInfoType> method;
	private StubArguments<ArgumentInfoType> arguments = new StubArguments<ArgumentInfoType>() {
		private final Object[] args = new Object[0];

		@SuppressWarnings("unchecked")
		@Override
		public <A> A getArgument(int index) {
			return (A) args[index];
		}
	};

	public OngoingStubBuilder(MethodInformation<ReceiverType, ReturnInfoType> method) {
		this.method = method;
	}

	@Override
	public OngoingStubbing<ReceiverType, ReturnInfoType> then(Answer<ReceiverType, ReturnInfoType> answer) {
		answers.add(answer);
		return this;
	}

	@Override
	public StubBuilder<ReceiverType, ReturnInfoType> with(StubArguments<ArgumentInfoType> arguments) {
		this.arguments = arguments;
		return this;
	}

	@Override
	public MethodInformation<ReceiverType, ?> getMethod() {
		return method;
	}

	@Override
	public StubArguments<?> getArguments() {
		return arguments;
	}

	@Override
	public List<Answer<ReceiverType, ?>> getAnswers() {
		return answers.stream().map(it -> (Answer<ReceiverType, ?>) it).collect(Collectors.toList());
	}
}
