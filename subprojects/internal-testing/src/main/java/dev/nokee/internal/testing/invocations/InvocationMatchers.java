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
package dev.nokee.internal.testing.invocations;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public final class InvocationMatchers {
	private InvocationMatchers() {}

	public static <T extends HasInvocationResults<?>> Matcher<T> calledOnce() {
		return called(equalTo(1L));
	}

	public static <T extends HasInvocationResults<?>> Matcher<T> called(Matcher<? super Long> matcher) {
		return new FeatureMatcher<T, Long>(matcher, "", "") {
			@Override
			protected Long featureValueOf(T actual) {
				return (long) actual.getAllInvocations().size();
			}
		};
	}

	public static <T extends HasInvocationResults<?>> Matcher<T> neverCalled() {
		return called(equalTo(0L));
	}

	public static <T extends HasInvocationResults<A>, A extends InvocationResult1<A0>, A0> Matcher<T> calledOnceWith(A0 expectedValue) {
		return calledOnceWith(equalTo(expectedValue));
	}

	@SuppressWarnings("unchecked")
	public static <T extends HasInvocationResults<A>, A extends InvocationResult1<A0>, A0> Matcher<T> calledOnceWith(Matcher<? super A0> matcher) {
		return allOf(calledOnce(), lastArguments(contains((Matcher<Object>) matcher)));
	}

	@SuppressWarnings("unchecked")
	public static <T extends HasInvocationResults<A>, A extends InvocationResult2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(Matcher<? super A0> matcher0, Matcher<? super A1> matcher1) {
		return allOf(calledOnce(), lastArguments(contains((Matcher<Object>) matcher0, (Matcher<Object>) matcher1)));
	}

	public static <T extends HasInvocationResults<A>, A extends InvocationResult> Matcher<T> lastArguments(Matcher<? super A> matcher) {
		return new FeatureMatcher<T, A>(matcher, "", "") {
			@Override
			protected A featureValueOf(T actual) {
				return actual.getAllInvocations().get(actual.getAllInvocations().size() - 1);
			}
		};
	}
}