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
import lombok.val;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class MockitoBuilder<T> implements TestDouble<T> {
	private final Class<T> classToMock;
	private final List<StubCall<T>> stubs = new ArrayList<>();
	private MockSettings settings = Mockito.withSettings();
	private T instance;

	private MockitoBuilder(Class<T> classToMock) {
		this.classToMock = classToMock;
	}

	public static <THIS, R extends ReturnInformation, A extends ArgumentInformation> StubBuilder<THIS, R> any(StubBuilder.WithArguments<THIS, R, A> stub) {
		return stub.with(anyArgs());
	}

	public static <ArgumentInfoType extends ArgumentInformation> StubArguments<ArgumentInfoType> anyArgs() {
		return new AnyStubArgs<>();
	}

	@Override
	public MockitoBuilder<T> when(StubCall<T> result) {
		stubs.add(result);
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public TestDouble<T> alwaysThrows() {
		this.settings = settings.defaultAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new UnsupportedOperationException();
			}
		});
		return this;
	}

	@Override
	public <R extends T> R instance() {
		if (instance == null) {
			final T instance = Mockito.mock(classToMock, settings);
			stubs.forEach(it -> {
				try {
					final Method method = it.getMethod().resolve(classToMock);

					T mock = Mockito.doAnswer(new Answer<Object>() {
						@Override
						public Object answer(InvocationOnMock invocation) throws Throwable {
							return it.getAnswers().get(0).answer(new InvocationOnTestDouble<T>() {
								@SuppressWarnings("unchecked")
								@Override
								public T getTestDouble() {
									return (T) invocation.getMock();
								}

								@Override
								public Object[] getArguments() {
									return invocation.getArguments();
								}
							});
						}
					}).when(instance);

					Object[] args = new Object[method.getParameterCount()];
					for (int i = 0; i < args.length; i++) {
						args[i] = it.getArguments().getArgument(i);
					}

					method.invoke(mock, args);
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			this.instance = instance;
		}
		@SuppressWarnings("unchecked")
		val result = (R) instance;
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T, R extends T> TestDouble<R> newMock(Class<T> classToMock) {
		return new MockitoBuilder<>((Class<R>) classToMock);
	}

	public static <T> T newAlwaysThrowingMock(Class<T> classToMock) {
		return newMock(classToMock).alwaysThrows().instance();
	}

	public static final class AnyStubArgs<ArgumentInfoType extends ArgumentInformation> implements StubArguments<ArgumentInfoType> {
		@Override
		public <A> A getArgument(int index) {
			return Mockito.any();
		}
	}
}
