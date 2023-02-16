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

import com.google.common.collect.Lists;
import lombok.val;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.mockito.Mockito;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ForwardingTestUtils {
	public static Object[] getParameterValues(Method method) {
		FreshValueGenerator paramValues = new FreshValueGenerator();
		final List<Object> passedArgs = Lists.newArrayList();
		for (Class<?> paramType : method.getParameterTypes()) {
			passedArgs.add(paramValues.generateFresh(paramType));
		}
		return passedArgs.toArray();
	}

	static Matcher<InteractionResult> returnValueForwarded() {
		return new TypeSafeMatcher<InteractionResult>() {
			@Override
			protected boolean matchesSafely(InteractionResult item) {
				// If we think this might be a 'chaining' call then we allow the return value to either
				// be the wrapper or the returnValue.
				return (!item.isPossibleChainingCall() || item.wrapper() != item.actualReturnValue()) && Objects.equals(item.returnValue(), item.actualReturnValue());
			}

			// mismatch...
			//assertEquals("Return value of " + method + " not forwarded", returnValue, actualReturnValue);


			@Override
			protected void describeMismatchSafely(InteractionResult item, Description mismatchDescription) {
				if (!item.isPossibleChainingCall() || item.wrapper() != item.actualReturnValue()) {
					mismatchDescription.appendText("did not forward return value of " + item.methodUnderTest());
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("forwards return value");
			}
		};
	}

	static Matcher<InteractionResult> calledMethodOnceWithPassedParameters() {
		return new TypeSafeMatcher<InteractionResult>() {
			@Override
			protected boolean matchesSafely(InteractionResult item) {
				try {
					if (item.expectedParameters().length == 0) {
						item.methodUnderTest().invoke(Mockito.verify(item.delegate()));
					} else if (item.expectedParameters().length == 1) {
						item.methodUnderTest().invoke(Mockito.verify(item.delegate()), item.expectedParameters()[0]);
					} else {
						item.methodUnderTest().invoke(Mockito.verify(item.delegate()), item.expectedParameters());
					}
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				return true;
			}

			@Override
			protected void describeMismatchSafely(InteractionResult item, Description mismatchDescription) {
				val mode = new CaptureInvocationCountVerificationMode();

				try {
					if (item.expectedParameters().length == 0) {
						item.methodUnderTest().invoke(Mockito.verify(item.delegate(), mode));
					} else if (item.expectedParameters().length == 1) {
						item.methodUnderTest().invoke(Mockito.verify(item.delegate(), mode), Mockito.any());
					} else {
						Object[] anyParams = new Object[item.expectedParameters().length];
						Arrays.fill(anyParams, Mockito.any());
						item.methodUnderTest().invoke(Mockito.verify(item.delegate(), mode), anyParams);
					}
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				mismatchDescription.appendText("called " + mode.count);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("delegates to method with passed parameters");
			}
		};
	}

	private static final class CaptureInvocationCountVerificationMode implements VerificationMode {
		int count;

		@Override
		public void verify(VerificationData data) {
			count = data.getAllInvocations().size();
		}
	}
}
