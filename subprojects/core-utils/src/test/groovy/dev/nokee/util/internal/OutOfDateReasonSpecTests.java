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
package dev.nokee.util.internal;

import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.internal.testing.MockitoMethodWrapper.method;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OutOfDateReasonSpecTests {
	@Mock Spec<Task> delegate;
	@Mock OutOfDateReasonSpec.InformationLogger logger;
	OutOfDateReasonSpec<Task> subject;
	@Mock Task task;

	@BeforeEach
	void givenSubject() {
		subject = new OutOfDateReasonSpec<>("some reason", delegate, logger);
	}

	@Nested
	class WhenDelegateIsSatisfied {
		boolean result;

		@BeforeEach
		void givenDelegateReturnsTrue() {
			Mockito.when(delegate.isSatisfiedBy(any())).thenReturn(true);
			result = subject.isSatisfiedBy(task);
		}

		@Test
		void doesNotLogAnything() {
			assertThat(method(logger, OutOfDateReasonSpec.InformationLogger::log), neverCalled());
		}

		@Test
		void forwardsIsSatisfiedToDelegate() {
			assertThat(method(delegate, Spec::isSatisfiedBy), calledOnceWith(task));
			assertThat(result, is(true));
		}
	}

	@Nested
	class WhenDelegateIsUnsatisfied {
		boolean result;

		@BeforeEach
		void givenDelegateReturnsFalse() {
			Mockito.when(delegate.isSatisfiedBy(any())).thenReturn(false);
			result = subject.isSatisfiedBy(task);
		}

		@Test
		void logsReason() {
			assertThat(method(logger, OutOfDateReasonSpec.InformationLogger::log),
				calledOnceWith("Task outputs are out-of-date because some reason."));
		}

		@Test
		void forwardsIsSatisfiedToDelegate() {
			assertThat(method(delegate, Spec::isSatisfiedBy), calledOnceWith(task));
			assertThat(result, is(false));
		}
	}
}
