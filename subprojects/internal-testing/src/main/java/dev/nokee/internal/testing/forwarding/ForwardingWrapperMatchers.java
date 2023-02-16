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
import dev.nokee.internal.testing.reflect.ReturnInformation;
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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ForwardingWrapperMatchers {
	public static <T> Matcher<ForwardingWrapperEx<T, T>> forwardsToDelegate(MethodInformation<T, ?> methodInfo) {
		return allOf(forwardsParametersToDelegate(methodInfo), forwardsExceptionsToDelegate(methodInfo));
	}

	public static <ObjectType, ReturnInfoType extends ReturnInformation> ForwardingMatcher<ObjectType, ReturnInfoType> forwards(MethodInformation<ObjectType, ReturnInfoType> fromMethodInfo) {
		return new ForwardingMatcher<>(fromMethodInfo);
	}

	public static final class ForwardingMatcher<ObjectType, ReturnInfoType extends ReturnInformation> {
		private final MethodInformation<ObjectType, ReturnInfoType> fromMethodInfo;

		public ForwardingMatcher(MethodInformation<ObjectType, ReturnInfoType> fromMethodInfo) {
			this.fromMethodInfo = fromMethodInfo;
		}

		public <DelegateType> Matcher<ForwardingWrapperEx<DelegateType, ObjectType>> to(MethodInformation<DelegateType, ReturnInfoType> toMethodInfo) {
			return allOf(new InteractionMatcher<>(fromMethodInfo, toMethodInfo), new ExceptionMatcher<>(fromMethodInfo, toMethodInfo));
		}

		public Matcher<ForwardingWrapperEx<ObjectType, ObjectType>> toDelegate() {
			return forwardsToDelegate(fromMethodInfo);
		}
	}

	public static <T> Matcher<ForwardingWrapperEx<T, T>> forwardsParametersToDelegate(MethodInformation<T, ?> methodInfo) {
		return new InteractionMatcher<>(methodInfo, methodInfo);
	}

	public static <T> Matcher<ForwardingWrapperEx<T, T>> forwardsExceptionsToDelegate(MethodInformation<T, ?> methodInfo) {
		return new ExceptionMatcher<>(methodInfo, methodInfo);
	}

	private static class ExceptionMatcher<ObjectType, DelegateType> extends TypeSafeMatcher<ForwardingWrapperEx<DelegateType, ObjectType>> {
		private final MethodInformation<ObjectType, ?> fromMethodInfo;
		private final MethodInformation<DelegateType, ?> toMethodInfo;

		public ExceptionMatcher(MethodInformation<ObjectType, ?> fromMethodInfo, MethodInformation<DelegateType, ?> toMethodInfo) {
			this.fromMethodInfo = fromMethodInfo;
			this.toMethodInfo = toMethodInfo;
		}

		@Override
		protected boolean matchesSafely(ForwardingWrapperEx<DelegateType, ObjectType> actual) {
			Method toMethod = toMethodInfo.resolve(actual.getForwardType());
			Object[] passedArgs = ForwardingTestUtils.getParameterValues(toMethod);

			final RuntimeException exception = new RuntimeException();
			DelegateType proxy = Mockito.mock(actual.getForwardType(), new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					assertEquals(toMethod, invocation.getMethod());
					throw exception;
				}
			});
			final ObjectType wrapper = actual.wrap(proxy);

			@SuppressWarnings("unchecked")
			final Class<ObjectType> objectType = (Class<ObjectType>) wrapper.getClass();
			Method fromMethod = fromMethodInfo.resolve(objectType);
			assertArrayEquals(fromMethod.getParameterTypes(), toMethod.getParameterTypes());

			try {
				fromMethod.invoke(wrapper, passedArgs);
				Assertions.fail(fromMethod + " failed to throw exception as is.");
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
	}
}
