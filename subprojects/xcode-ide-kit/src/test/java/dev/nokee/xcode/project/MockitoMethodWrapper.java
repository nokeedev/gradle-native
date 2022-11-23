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
package dev.nokee.xcode.project;

import com.google.common.collect.Iterators;
import dev.nokee.xcode.project.invocations.HasInvocationResults;
import dev.nokee.xcode.project.invocations.InvocationResult;
import dev.nokee.xcode.project.invocations.InvocationResult0;
import dev.nokee.xcode.project.invocations.InvocationResult1;
import dev.nokee.xcode.project.invocations.InvocationResult2;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class MockitoMethodWrapper<A extends InvocationResult> implements HasInvocationResults<A> {
	private final List<A> invocations;

	public MockitoMethodWrapper(List<A> invocations) {
		this.invocations = invocations;
	}

	@Override
	public List<A> getAllInvocations() {
		return invocations;
	}

	static <T> MockitoMethodWrapper<InvocationResult0> method(T mock, A0<? super T> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())));
		return new MockitoMethodWrapper<>(invocationList.stream().map(DefaultInvocationResult::new).collect(Collectors.toList()));
	}

	interface A0<MockType> {
		void call(MockType self);
	}

	static <T, A0> MockitoMethodWrapper<InvocationResult1<A0>> method(T mock, A1<T, A0> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())), Mockito.any());
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<A0, Void>(it)).collect(Collectors.toList()));
	}

	interface A1<MockType, A0> {
		void call(MockType self, A0 a0);
	}

	static <T, A0, A1> MockitoMethodWrapper<InvocationResult2<A0, A1>> method(T mock, A2<T, A0, A1> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())), Mockito.any(), Mockito.any());
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<A0, A1>(it)).collect(Collectors.toList()));
	}

	interface A2<MockType, A0, A1> {
		void call(MockType self, A0 a0, A1 a1);
	}

	private static final class DefaultInvocationResult<A0, A1> implements InvocationResult0, InvocationResult1<A0>, InvocationResult2<A0, A1> {
		private final Invocation invocation;

		private DefaultInvocationResult(Invocation invocation) {
			this.invocation = invocation;
		}

		@Override
		public <A> A getArgument(int index) {
			return invocation.getArgument(index);
		}

		@Override
		public <A> A getArgument(int index, Class<A> argumentType) {
			return invocation.getArgument(index, argumentType);
		}

		@Override
		public A0 getFirstArgument() {
			return invocation.getArgument(0);
		}

		@Override
		public A1 getSecondArgument() {
			return invocation.getArgument(1);
		}

		@Override
		public Iterator<Object> iterator() {
			return Iterators.forArray(invocation.getArguments());
		}
	}
}
