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
package dev.nokee.internal.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import dev.nokee.internal.testing.invocations.HasInvocationResults;
import dev.nokee.internal.testing.invocations.InvocationResult;
import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.internal.testing.reflect.MethodCallable;
import dev.nokee.internal.testing.reflect.MethodInformation;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class MockitoMethodWrapper<A extends ArgumentInformation> implements HasInvocationResults<A> {
	private final List<InvocationResult<A>> invocations;

	public MockitoMethodWrapper(List<InvocationResult<A>> invocations) {
		this.invocations = invocations;
	}

	@Override
	public List<InvocationResult<A>> getAllInvocations() {
		return invocations;
	}

	@SuppressWarnings({"unchecked", "overloads"})
	public static <T> MockitoMethodWrapper<ArgumentInformation.None> method(T mock, MethodInformation<T, ?> mapper) {
		final List<Invocation> invocationList = new ArrayList<>();
		final Method method = mapper.resolve((Class<T>) Mockito.mockingDetails(mock).getMockHandler().getMockSettings().getTypeToMock());
		Mockito.mockingDetails(mock).getInvocations().stream().filter(it -> it.getMethod().equals(method)).forEach(invocationList::add);
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<ArgumentInformation.None>(it)).collect(Collectors.toList()));
	}

	@SuppressWarnings({"unchecked", "overloads"})
	public static <T, A extends ArgumentInformation> MockitoMethodWrapper<A> method(T mock, MethodInformation.WithArguments<T, ?, A> mapper) {
		final List<Invocation> invocationList = new ArrayList<>();
		final Method method = mapper.resolve((Class<T>) Mockito.mockingDetails(mock).getMockHandler().getMockSettings().getTypeToMock());
		Mockito.mockingDetails(mock).getInvocations().stream().filter(it -> it.getMethod().equals(method)).forEach(invocationList::add);
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<A>(it)).collect(Collectors.toList()));
	}

	@SuppressWarnings("overloads")
	public static <T> MockitoMethodWrapper<ArgumentInformation.None> method(T mock, MethodCallable.ForArg0<T, RuntimeException> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())));
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<ArgumentInformation.None>(it)).collect(Collectors.toList()));
	}

	public static <T, A0> MockitoMethodWrapper<ArgumentInformation.Arg1<A0>> method(T mock, MethodCallable.ForArg1<T, A0, RuntimeException> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())), Mockito.any());
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<ArgumentInformation.Arg1<A0>>(it)).collect(Collectors.toList()));
	}

	public static <T, A0, A1> MockitoMethodWrapper<ArgumentInformation.Arg2<A0, A1>> method(T mock, MethodCallable.ForArg2<T, A0, A1, RuntimeException> mapper) {
		List<Invocation> invocationList = new ArrayList<>();
		mapper.call(Mockito.verify(mock, data -> invocationList.addAll(data.getAllInvocations())), Mockito.any(), Mockito.any());
		return new MockitoMethodWrapper<>(invocationList.stream().map(it -> new DefaultInvocationResult<ArgumentInformation.Arg2<A0, A1>>(it)).collect(Collectors.toList()));
	}

	private static final class DefaultInvocationResult<ArgumentInformationType extends ArgumentInformation> implements InvocationResult<ArgumentInformationType> {
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
		public Iterator<Object> iterator() {
			return Iterators.forArray(invocation.getArguments());
		}

		@Override
		public String toString() {
			return "invocation of " + invocation.getMethod() + " with " + ImmutableList.copyOf(invocation.getArguments());
		}
	}
}
