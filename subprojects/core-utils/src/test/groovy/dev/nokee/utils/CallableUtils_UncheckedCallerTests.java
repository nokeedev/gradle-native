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
package dev.nokee.utils;

import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;

import static dev.nokee.utils.CallableUtils.DEFAULT_EXCEPTION_HANDLER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CallableUtils_UncheckedCallerTests {
	@Test
	void createUncheckedCallerUsingDefaultExceptionHandler() {
		assertThat(CallableUtils.unchecked(), equalTo(new CallableUtils.DefaultUncheckedCaller(DEFAULT_EXCEPTION_HANDLER)));
	}

	@Nested
	class GivenSubject {
		CallableUtils.ExceptionHandler handler = Mockito.spy(new CallableUtils.ExceptionHandler() {
			@Override
			public RuntimeException rethrowAsUncheckedException(Throwable t) {
				return new RuntimeException(t); // crappy implementation for testing
			}
		});
		@InjectMocks CallableUtils.DefaultUncheckedCaller subject;
		@Mock Callable<Object> target;

		@Nested
		class WhenCallableThrowsCheckedException {
			CheckedException checkedException = new CheckedException();

			@BeforeEach
			void givenCallThrowsCheckedException() throws Exception {
				Mockito.when(target.call()).thenThrow(checkedException);
			}

			@Test
			void callsExceptionHandlerWithCheckedException() {
				discardAnyException(() -> subject.call(target));
				Mockito.verify(handler).rethrowAsUncheckedException(checkedException);
			}
		}

		@Nested
		class WhenCallableThrowsUncheckedException {
			UncheckedException uncheckedException = new UncheckedException();

			@BeforeEach
			void givenCallThrowsUncheckedException() throws Exception {
				Mockito.when(target.call()).thenThrow(uncheckedException);
			}

			@Test
			void callsExceptionHandlerWithUncheckedException() {
				discardAnyException(() -> subject.call(target));
				Mockito.verify(handler).rethrowAsUncheckedException(uncheckedException);
			}
		}

		@Nested
		class WhenCallableReturnsWithoutException {
			Object returnValue = new Object();

			@BeforeEach
			void givenCallThrowsCheckedException() throws Exception {
				Mockito.when(target.call()).thenReturn(returnValue);
			}

			@Test
			void doesNotCallExceptionHandler() throws Exception {
				ignoresReturnValue(target.call());
				Mockito.verifyNoInteractions(handler);
			}

			@Test
			void returnsCallableValue() throws Exception {
				assertThat(target.call(), equalTo(returnValue));
			}
		}
	}

	private static <R> void ignoresReturnValue(R returnValue) {
		// do nothing with value...
	}
	private static final class CheckedException extends Exception {}
	private static final class UncheckedException extends RuntimeException {}

	private static <E extends Throwable> void discardAnyException(FailableRunnable<E> runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {
			// ignores
		}
	}
}
