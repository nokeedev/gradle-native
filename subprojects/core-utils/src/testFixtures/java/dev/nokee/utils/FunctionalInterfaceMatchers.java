package dev.nokee.utils;

import com.google.common.collect.Iterators;
import groovy.lang.Closure;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

public final class FunctionalInterfaceMatchers {
	public static <T extends ExecutionArguments> Matcher<HasExecutionResult<T>> calledOnceWith(Matcher<? super T> matcher) {
		return allOf(called(equalTo(1L)), lastArgument(matcher));
	}

	public static <T> Matcher<ExecutionArgument<T>> singleArgumentOf(T obj) {
		return singleArgumentOf(equalTo(obj));
	}

	public static <T> Matcher<ExecutionArgument<T>> singleArgumentOf(Matcher<? super T> matcher) {
		return new FeatureMatcher<ExecutionArgument<T>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(ExecutionArgument<T> actual) {
				return actual.get();
			}
		};
	}

	public static <T, U> Matcher<ExecutionBiArguments<T, U>> firstArgumentOf(T instance) {
		return firstArgumentOf(equalTo(instance));
	}

	public static <T, U> Matcher<ExecutionBiArguments<T, U>> firstArgumentOf(Matcher<? super T> matcher) {
		return new FeatureMatcher<ExecutionBiArguments<T, U>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(ExecutionBiArguments<T, U> actual) {
				return actual.getFirst();
			}
		};
	}

	public static <T, U> Matcher<ExecutionBiArguments<T, U>> secondArgumentOf(U instance) {
		return secondArgumentOf(equalTo(instance));
	}

	public static <T, U> Matcher<ExecutionBiArguments<T, U>> secondArgumentOf(Matcher<? super U> matcher) {
		return new FeatureMatcher<ExecutionBiArguments<T, U>, U>(matcher, "", "") {
			@Override
			protected U featureValueOf(ExecutionBiArguments<T, U> actual) {
				return actual.getSecond();
			}
		};
	}

	public static <T extends ExecutionArguments> Matcher<HasExecutionResult<T>> called(Matcher<? super Long> callCountMatcher) {
		return new FeatureMatcher<HasExecutionResult<T>, Long>(callCountMatcher, "", "") {
			@Override
			protected Long featureValueOf(HasExecutionResult<T> actual) {
				return ExecutionResult.from(actual).getArguments().count();
			}
		};
	}

	public static <T extends ExecutionArguments> Matcher<HasExecutionResult<T>> calledOnce() {
		return called(equalTo(1L));
	}

	public static <T extends ExecutionArguments> Matcher<HasExecutionResult<T>> neverCalled() {
		return called(equalTo(0L));
	}

	public static <T extends ExecutionArguments> Matcher<HasExecutionResult<T>> lastArgument(Matcher<? super T> matcher) {
		return new FeatureMatcher<HasExecutionResult<T>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(HasExecutionResult<T> actual) {
				return Iterators.getLast(ExecutionResult.from(actual).getArguments().iterator());
			}
		};
	}

	public static Matcher<ClosureExecutionDelegate> delegateOf(Object delegateInstance) {
		return delegateOf(equalTo(delegateInstance));
	}

	public static Matcher<ClosureExecutionDelegate> delegateOf(Matcher<? super Object> delegateMatcher) {
		return new FeatureMatcher<ClosureExecutionDelegate, Object>(delegateMatcher, "", "") {
			@Override
			protected Object featureValueOf(ClosureExecutionDelegate actual) {
				return actual.getDelegate();
			}
		};
	}

	public static Matcher<ClosureExecutionDelegate> delegateFirstStrategy() {
		return new FeatureMatcher<ClosureExecutionDelegate, Integer>(equalTo(Closure.DELEGATE_FIRST), "", "") {
			@Override
			protected Integer featureValueOf(ClosureExecutionDelegate actual) {
				return actual.getResolveStrategy();
			}
		};
	}
}
