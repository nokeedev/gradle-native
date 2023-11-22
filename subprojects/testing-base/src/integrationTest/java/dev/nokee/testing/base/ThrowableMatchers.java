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

package dev.nokee.testing.base;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.hasItem;

public final class ThrowableMatchers {
	private static Invocation callTo(ThrowingRunnable runnable) {
		return new Invocation(runnable);
	}

	static Invocation callTo(Callable<?> callable) {
		return new Invocation(callable::call);
	}

	static Matcher<Invocation> throwsException(Matcher<? super Throwable> matcher) {
		return new FeatureMatcher<Invocation, Throwable>(matcher, "", "") {
			@Override
			protected Throwable featureValueOf(Invocation actual) {
				try {
					actual.runnable.run();
					return Assertions.fail("exception expected, none was thrown");
				} catch (Throwable e) {
					return e;
				}
			}
		};
	}

	static Matcher<Invocation> doesNotThrowException() {
		return new TypeSafeMatcher<Invocation>() {
			@Override
			protected boolean matchesSafely(Invocation actual) {
				try {
					actual.runnable.run();
					return true;
				} catch (Throwable e) {
					return false; // TODO: Describe that no exception was expected
				}
			}

			@Override
			public void describeTo(Description description) {

			}
		};
	}

	static Matcher<Throwable> message(String message) {
		return new FeatureMatcher<Throwable, List<String>>(hasItem(message), "", "") {
			@Override
			protected List<String> featureValueOf(Throwable actual) {
				final List<String> failureMessages = new ArrayList<>();
				do {
					failureMessages.add(actual.getMessage());
				} while ((actual = actual.getCause()) != null);

				return failureMessages;
			}
		};
	}

	private static class Invocation {
		private final ThrowingRunnable runnable;

		private Invocation(ThrowingRunnable runnable) {
			this.runnable = runnable;
		}
	}
}
