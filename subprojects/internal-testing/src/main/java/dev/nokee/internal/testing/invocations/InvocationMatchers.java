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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.internal.testing.testdoubles.MethodVerifier;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import groovy.lang.Closure;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

public final class InvocationMatchers {
	private InvocationMatchers() {}

	public static <T extends HasInvocationResults<?>> Matcher<T> calledOnce() {
		return called(equalTo(1L));
	}

	public static <T extends HasInvocationResults<A>, R extends InvocationResult<A>, A extends ArgumentInformation> Matcher<T> calledOnce(Matcher<? super R> matcher) {
		return allOf(calledOnce(), lastInvocation(matcher));
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

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg1<A0>, A0> Matcher<T> calledOnceWith(A0 expectedValue) {
		return calledOnceWith(equalTo(expectedValue));
	}

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg1<A0>, A0> Matcher<T> calledOnceWith(TestDouble<A0> expectedValue) {
		return calledOnceWith(equalTo(expectedValue.instance()));
	}

	@SuppressWarnings("unchecked")
	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg1<A0>, A0> Matcher<T> calledOnceWith(Matcher<? super A0> matcher) {
		return allOf(calledOnce(), lastInvocation(contains((Matcher<Object>) matcher)));
	}

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(A0 instance0, A1 instance1) {
		return calledOnceWith(equalTo(instance0), equalTo(instance1));
	}

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(A0 instance0, TestDouble<A1> instance1) {
		return calledOnceWith(equalTo(instance0), equalTo(instance1.instance()));
	}

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(TestDouble<A0> instance0, A1 instance1) {
		return calledOnceWith(equalTo(instance0.instance()), equalTo(instance1));
	}

	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(TestDouble<A0> instance0, TestDouble<A1> instance1) {
		return calledOnceWith(equalTo(instance0.instance()), equalTo(instance1.instance()));
	}

	@SuppressWarnings("unchecked")
	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<T> calledOnceWith(Matcher<? super A0> matcher0, Matcher<? super A1> matcher1) {
		return allOf(calledOnce(), lastInvocation(contains((Matcher<Object>) matcher0, (Matcher<Object>) matcher1)));
	}

	public static <T extends HasInvocationResults<A>, R extends InvocationResult<A>, A extends ArgumentInformation> Matcher<T> lastInvocation(Matcher<? super R> matcher) {
		return new FeatureMatcher<T, R>(matcher, "", "") {
			@Override
			@SuppressWarnings("unchecked")
			protected R featureValueOf(T actual) {
				return (R) actual.getAllInvocations().get(actual.getAllInvocations().size() - 1);
			}
		};
	}

	@SuppressWarnings("varargs")
	@SafeVarargs
	public static <T extends HasInvocationResults<A>, A extends ArgumentInformation> Matcher<T> called(Matcher<? super InvocationResult<A>>... matcher) {
		return new FeatureMatcher<T, Iterable<InvocationResult<A>>>(contains(ImmutableList.copyOf(matcher)), "", "") {
			@Override
			protected Iterable<InvocationResult<A>> featureValueOf(T actual) {
				return ImmutableList.copyOf(actual.getAllInvocations());
			}
		};
	}

	public static <A extends ArgumentInformation.Arg1<A0>, A0> Matcher<InvocationResult<A>> with(A0 expectedValue) {
		return with(equalTo(expectedValue));
	}

	@SuppressWarnings("unchecked")
	public static <A extends ArgumentInformation.Arg1<A0>, A0> Matcher<InvocationResult<A>> with(Matcher<? super A0> matcher) {
		return new FeatureMatcher<InvocationResult<A>, List<?>>(contains(ImmutableList.of((Matcher<Object>) matcher)), "", "") {
			@Override
			protected List<?> featureValueOf(InvocationResult<A> actual) {
				return ImmutableList.copyOf(actual);
			}
		};
	}

	public static <A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<InvocationResult<A>> with(A0 instance0, A1 instance1) {
		return with(equalTo(instance0), equalTo(instance1));
	}

	@SuppressWarnings("unchecked")
	public static <A extends ArgumentInformation.Arg2<A0, A1>, A0, A1> Matcher<InvocationResult<A>> with(Matcher<? super A0> matcher0, Matcher<? super A1> matcher1) {
		return new FeatureMatcher<InvocationResult<A>, List<?>>(contains((Matcher<Object>) matcher0, (Matcher<Object>) matcher1), "", "") {
			@Override
			protected List<?> featureValueOf(InvocationResult<A> actual) {
				return ImmutableList.copyOf(actual);
			}
		};
	}

	public static <A extends ArgumentInformation.Arg3<A0, A1, A2>, A0, A1, A2> Matcher<InvocationResult<A>> with(A0 instance0, A1 instance1, A2 instance2) {
		return with(equalTo(instance0), equalTo(instance1), equalTo(instance2));
	}

	@SuppressWarnings("unchecked")
	public static <A extends ArgumentInformation.Arg3<A0, A1, A2>, A0, A1, A2> Matcher<InvocationResult<A>> with(Matcher<? super A0> matcher0, Matcher<? super A1> matcher1, Matcher<? super A2> matcher2) {
		return new FeatureMatcher<InvocationResult<A>, List<?>>(contains((Matcher<Object>) matcher0, (Matcher<Object>) matcher1, (Matcher<Object>) matcher2), "", "") {
			@Override
			protected List<?> featureValueOf(InvocationResult<A> actual) {
				return ImmutableList.copyOf(actual);
			}
		};
	}

	public static <A extends ArgumentInformation> Matcher<MethodVerifier.MyInvocationResult<A>> withCaptured(Object value) {
		return withCaptured(equalTo(value));
	}

	public static <A extends ArgumentInformation, T> Matcher<MethodVerifier.MyInvocationResult<A>> withCaptured(Matcher<T> matcher) {
		return new FeatureMatcher<MethodVerifier.MyInvocationResult<A>, List<Object>>(hasItem(matcher), "", "") {
			@Override
			protected List<Object> featureValueOf(MethodVerifier.MyInvocationResult<A> actual) {
				return actual.getCapturedData();
			}
		};
	}

	public static <A extends ArgumentInformation.Arg3<Object, Integer, Object[]>> Matcher<InvocationResult<A>> withClosureArguments(Object... instances) {
		return with(any(Object.class), any(Integer.class), arrayContaining(instances));
	}

	@SafeVarargs
	@SuppressWarnings({"varargs", "unchecked"})
	public static <A extends ArgumentInformation.Arg3<Object, Integer, Object[]>, E> Matcher<InvocationResult<A>> withClosureArguments(Matcher<? super E>... matchers) {
		return with(any(Object.class), any(Integer.class), arrayContaining(ImmutableList.copyOf((Matcher<Object>[]) matchers)));
	}

	@SuppressWarnings("unchecked")
	public static <A extends ArgumentInformation.Arg3<Object, Integer, Object[]>, T> Matcher<InvocationResult<A>> withDelegateOf(Matcher<T> matcher) {
		return with((Matcher<Object>) matcher, any(Integer.class), any(Object[].class));
	}

	public static <A extends ArgumentInformation.Arg3<Object, Integer, Object[]>> Matcher<InvocationResult<A>> withDelegateOf(Object instance) {
		return withDelegateOf(equalTo(instance));
	}

	public static <A extends ArgumentInformation.Arg3<Object, Integer, Object[]>> Matcher<InvocationResult<A>> withDelegateFirstStrategy() {
		return with(any(Object.class), equalTo(Closure.DELEGATE_FIRST), any(Object[].class));
	}
}
