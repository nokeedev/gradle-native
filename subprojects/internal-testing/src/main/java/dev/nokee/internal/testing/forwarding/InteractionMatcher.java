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
package dev.nokee.internal.testing.forwarding;

import dev.nokee.internal.testing.reflect.MethodInformation;
import org.hamcrest.FeatureMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class InteractionMatcher<ObjectType, DelegateType> extends FeatureMatcher<ForwardingWrapperEx<DelegateType, ObjectType>, InteractionResult> {
	private final MethodInformation<ObjectType, ?> fromMethodInformation;
	private final MethodInformation<DelegateType, ?> toMethodInformation;

	public InteractionMatcher(MethodInformation<ObjectType, ?> fromMethodInformation, MethodInformation<DelegateType, ?> toMethodInformation) {
		super(allOf(ForwardingTestUtils.returnValueForwarded(), ForwardingTestUtils.calledMethodOnceWithPassedParameters()/*calledMethod(methodName), calledOnce(), */), "an interaction with " + toMethodInformation, "interaction with " + toMethodInformation);
		this.fromMethodInformation = fromMethodInformation;
		this.toMethodInformation = toMethodInformation;
	}

	@Override
	protected InteractionResult featureValueOf(ForwardingWrapperEx<DelegateType, ObjectType> actual) {
		Method toMethod = toMethodInformation.resolve(actual.getForwardType());
		Object[] passedArgs = ForwardingTestUtils.getParameterValues(toMethod);

		Object returnValue = new FreshValueGenerator().generateFresh(toMethod.getReturnType());

		DelegateType proxy = Mockito.mock(actual.getForwardType(), new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(toMethod, invocation.getMethod());
				return returnValue;
			}
		});
		final ObjectType wrapper = actual.wrap(proxy);

		@SuppressWarnings("unchecked")
		Class<ObjectType> objectType = (Class<ObjectType>) wrapper.getClass();
		final Method fromMethod = fromMethodInformation.resolve(objectType);
		assertArrayEquals(fromMethod.getParameterTypes(), toMethod.getParameterTypes());

		boolean isPossibleChainingCall = fromMethod.getDeclaringClass().isAssignableFrom(fromMethod.getReturnType());
		try {
			Object actualReturnValue = fromMethod.invoke(wrapper, passedArgs);
			return new InteractionResult() {
				@Override
				public boolean isPossibleChainingCall() {
					return isPossibleChainingCall;
				}

				@Override
				public Object wrapper() {
					return wrapper;
				}

				@Override
				public Object actualReturnValue() {
					return actualReturnValue;
				}

				@Override
				public Object returnValue() {
					return returnValue;
				}

				@Override
				public Object delegate() {
					return proxy;
				}

				@Override
				public Method methodUnderTest() {
					return toMethod;
				}

				@Override
				public Object[] expectedParameters() {
					return passedArgs;
				}

				@Override
				public Throwable thrownException() {
					return null;
				}
			};
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			return new InteractionResult() {
				@Override
				public boolean isPossibleChainingCall() {
					return isPossibleChainingCall;
				}

				@Override
				public Object wrapper() {
					return wrapper;
				}

				@Override
				public Object actualReturnValue() {
					return null;
				}

				@Override
				public Object returnValue() {
					return returnValue;
				}

				@Override
				public Object delegate() {
					return proxy;
				}

				@Override
				public Method methodUnderTest() {
					return toMethod;
				}

				@Override
				public Object[] expectedParameters() {
					return passedArgs;
				}

				@Override
				public Throwable thrownException() {
					return e.getCause();
				}
			};
		}
	}
}
