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
package dev.nokee.xcode.project.forwarding;

import dev.nokee.internal.testing.reflect.MethodInformation;
import org.hamcrest.FeatureMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class InteractionMatcher<T> extends FeatureMatcher<ForwardingWrapper<T>, InteractionResult> {
	private final MethodInformation<T, ?> methodInformation;

	public InteractionMatcher(MethodInformation<T, ?> methodInformation) {
		super(allOf(ForwardingTestUtils.returnValueForwarded(), ForwardingTestUtils.calledMethodOnceWithPassedParameters()/*calledMethod(methodName), calledOnce(), */), "an interaction with " + methodInformation, "interaction with " + methodInformation);
		this.methodInformation = methodInformation;
	}

	@Override
	protected InteractionResult featureValueOf(ForwardingWrapper<T> actual) {
		Method method = methodInformation.resolve(actual.getForwardType());
		Object[] passedArgs = ForwardingTestUtils.getParameterValues(method);


		Object returnValue = new FreshValueGenerator().generateFresh(method.getReturnType());

		T proxy = Mockito.mock(actual.getForwardType(), new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(method, invocation.getMethod());
				return returnValue;
			}
		});
		T wrapper = actual.wrap(proxy);
		boolean isPossibleChainingCall = actual.getForwardType().isAssignableFrom(method.getReturnType());
		try {
			Object actualReturnValue = method.invoke(wrapper, passedArgs);
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
					return method;
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
					return method;
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
