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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.MockitoMethodWrapper;
import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.internal.testing.reflect.MethodInformation;
import dev.nokee.internal.testing.reflect.ReturnInformation;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MockitoBuilder<T> implements TestDouble<T> {
	private final Class<T> classToMock;
	private final List<StubCall<T>> stubs = new ArrayList<>();
	private MockSettings settings = Mockito.withSettings();
	private T instance;
	private final Multimap<Method, List<Object>> captured = ArrayListMultimap.create();

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
	public MethodVerifier<ArgumentInformation.None> to(MethodInformation<T, ?> method) {
		return new MethodVerifier<ArgumentInformation.None>() {
			private final MockitoMethodWrapper<ArgumentInformation.None> delegate = MockitoMethodWrapper.method(instance(), method);
			private final Collection<List<Object>> data = captured.get(method.resolve(classToMock));

			@Override
			public List<MyInvocationResult<ArgumentInformation.None>> getAllInvocations() {
				return Streams.zip(delegate.getAllInvocations().stream(), data.stream(), (a, b) -> {
					return new MyInvocationResult<ArgumentInformation.None>() {
						@Override
						public Iterator<Object> iterator() {
							return a.iterator();
						}

						@Override
						public List<Object> getCapturedData() {
							return b;
						}
					};
				}).collect(Collectors.toList());
			}
		};
	}

	@Override
	public <A extends ArgumentInformation> MethodVerifier<A> to(MethodInformation.WithArguments<T, ?, A> method) {
		return new MethodVerifier<A>() {
			private final MockitoMethodWrapper<A> delegate = MockitoMethodWrapper.method(instance(), method);
			private final Collection<List<Object>> data = captured.get(method.resolve(classToMock));

			@Override
			public List<MyInvocationResult<A>> getAllInvocations() {
				Stream<List<Object>> dataStream = Stream.generate(ImmutableList::of);
				if (!data.isEmpty()) {
					dataStream = data.stream();
				}
				return Streams.zip(delegate.getAllInvocations().stream(), dataStream, (a, b) -> {
					return new MyInvocationResult<A>() {
						@Override
						public Iterator<Object> iterator() {
							return a.iterator();
						}

						@Override
						public List<Object> getCapturedData() {
							return b;
						}
					};
				}).collect(Collectors.toList());
			}
		};
	}

	@Override
	public T instance() {
		if (instance == null) {
			final T instance = Mockito.mock(classToMock, settings);
			// TODO: merge stubs for the same method together
			stubs.forEach(it -> {
				try {
					final Method method = it.getMethod().resolve(classToMock);

					T mock = Mockito.doAnswer(new Answer<Object>() {
						@Override
						public Object answer(InvocationOnMock invocation) throws Throwable {
							captured.put(invocation.getMethod(), it.getCaptors().stream().map(Captor::capture).collect(Collectors.toList()));

							if (it.getAnswers().isEmpty()) {
								return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
							}

							// TODO: Support multiple answers
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
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static <T, R extends T> TestDouble<R> newMock(Class<T> classToMock) {
		return new MockitoBuilder<>((Class<R>) classToMock);
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <T> TestDouble<T> newMock(TypeToken<T> classToMock) {
		return new MockitoBuilder<>((Class<T>) classToMock.getRawType());
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
