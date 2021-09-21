/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public abstract class AbstractMatcherTest {

	protected abstract Matcher<?> createMatcher();

	public static void assertNullSafe(Matcher<?> matcher) {
		assertDoesNotThrow(() -> matcher.matches(null), "matcher was not null safe");
	}

	public static <T> void assertMatches(Matcher<T> matcher, Object arg, String message) {
		assertTrue(matcher.matches(arg), message + " because: '" + mismatchDescription(matcher, arg) + "'");
	}

	public static <T> void assertDoesNotMatch(Matcher<? super T> c, T arg) {
		assertDoesNotMatch(c, arg, "Unexpected match");
	}

	public static <T> void assertDoesNotMatch(Matcher<? super T> c, T arg, String message) {
		assertFalse(c.matches(arg), message);
	}

	public static void assertDescription(String expected, Matcher<?> matcher) {
		Description description = new StringDescription();
		description.appendDescriptionOf(matcher);
		assertEquals(expected, description.toString().trim(), "Expected description");
	}

	public static <T> void assertMismatchDescription(String expected, Matcher<? super T> matcher, Object arg) {
		assertFalse(matcher.matches(arg), "Precondition: Matcher should not match item.");
		assertEquals(expected, mismatchDescription(matcher, arg), "expected mismatch description");
	}

	private static <T> String mismatchDescription(Matcher<? super T> matcher, Object arg) {
		Description description = new StringDescription();
		matcher.describeMismatch(arg, description);
		return description.toString().trim();
	}

	@Test
	void checkNullSafety() {
		assertNullSafe(createMatcher());
	}

	@Test
	void copesWithUnknownTypes() {
		createMatcher().matches(new UnknownType());
	}

	private static class UnknownType {}
}
