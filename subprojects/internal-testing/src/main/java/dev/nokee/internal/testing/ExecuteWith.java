package dev.nokee.internal.testing;

import com.google.common.collect.Iterables;
import groovy.lang.Closure;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gradle.api.Action;
import org.gradle.internal.Cast;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.utils.ClosureTestUtils.adaptToClosure;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

public final class ExecuteWith {
	private ExecuteWith() {}

	public static <T> ExecutionResult<T> executeWith(ExecutionStrategy<T> strategy) {
		try {
			return strategy.execute();
		} catch (Throwable throwable) {
			return ExceptionUtils.rethrow(throwable);
		}
	}

	public interface ExecutionResult<T> {
		T getLastArgument();
		int getCalledCount();
	}

	public interface ExecutionStrategy<T> {
		ExecutionResult<T> execute() throws Throwable;
	}

	public static <T> ExecutionStrategy<T> action(ThrowingConsumer<? super Action<? super T>> execution) {
		return new ExecutionStrategy<T>() {
			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Action<T> action = Cast.uncheckedCast(Mockito.mock(Action.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doNothing().when(action).execute(captor.capture());
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public static <T> ExecutionStrategy<T> consumer(ThrowingConsumer<? super Consumer<? super T>> execution) {
		return new ExecutionStrategy<T>() {
			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Consumer<T> action = Cast.uncheckedCast(Mockito.mock(Consumer.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doNothing().when(action).accept(captor.capture());
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	private static class ActionExecutionResult<T> implements ExecutionResult<T> {
		private final List<T> arguments;

		private ActionExecutionResult(List<T> arguments) {
			this.arguments = arguments;
		}

		public T getLastArgument() {
			return Iterables.getLast(arguments);
		}

		public int getCalledCount() {
			return arguments.size();
		}
	}

	public static <T> ExecutionStrategy<T> closure(ThrowingConsumer<? super Closure<Void>> execution) {
		return new ExecutionStrategy<T>() {
			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Action<T> action = Cast.uncheckedCast(Mockito.mock(Action.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doNothing().when(action).execute(captor.capture());
				execution.accept(adaptToClosure(action::execute));
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public static <T> Matcher<ExecutionResult<T>> calledOnceWith(Matcher<T> matcher) {
		return allOf(called(equalTo(1)), lastArgument(matcher));
	}

	public static <T> Matcher<ExecutionResult<T>> called(Matcher<Integer> matcher) {
		return new FeatureMatcher<ExecutionResult<T>, Integer>(matcher, "", "") {
			@Override
			protected Integer featureValueOf(ExecutionResult<T> actual) {
				return actual.getCalledCount();
			}
		};
	}

	public static <T> Matcher<ExecutionResult<T>> lastArgument(Matcher<T> matcher) {
		return new FeatureMatcher<ExecutionResult<T>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(ExecutionResult<T> actual) {
				return actual.getLastArgument();
			}
		};
	}
}
