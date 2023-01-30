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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ForwardingWrapperMatchers {
	public static <T> Matcher<ForwardingWrapper<T>> forwardsToDelegate(MethodInformation<T, ?> methodInfo) {
		return allOf(forwardsParametersToDelegate(methodInfo), forwardsExceptionsToDelegate(methodInfo));
	}

	public static <T> Matcher<ForwardingWrapper<T>> forwardsParametersToDelegate(MethodInformation<T, ?> methodInfo) {
		return new InteractionMatcher<>(methodInfo);
	}

	public static <T> Matcher<ForwardingWrapper<T>> forwardsExceptionsToDelegate(MethodInformation<T, ?> methodInfo) {
		return new TypeSafeMatcher<ForwardingWrapper<T>>() {
			@Override
			protected boolean matchesSafely(ForwardingWrapper<T> actual) {
				Method method = methodInfo.resolve(actual.getForwardType());
				Object[] passedArgs = ForwardingTestUtils.getParameterValues(method);

				final RuntimeException exception = new RuntimeException();
				T proxy = Mockito.mock(actual.getForwardType(), new Answer<Object>() {
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						assertEquals(method, invocation.getMethod());
						throw exception;
					}
				});
				T wrapper = actual.wrap(proxy);
				try {
					method.invoke(wrapper, passedArgs);
					Assertions.fail(method + " failed to throw exception as is.");
					return false; // appeasing the compiler: this line will never be executed.
				} catch (InvocationTargetException e) {
					if (exception != e.getCause()) {
						throw new RuntimeException(e);
					}
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {

			}
		};
	}
}
