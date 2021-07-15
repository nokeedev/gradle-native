package dev.nokee.utils;

import groovy.lang.Closure;
import lombok.EqualsAndHashCode;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;

public final class ClosureTestUtils {
	public static <R, T> MockClosure<R, T> mockClosure(Class<T> firstArgumentType) {
		return new MockClosure<R, T>();
	}

	public static final class MockClosure<R, T> extends Closure<R> implements HasExecutionResult<ExecutionClosureArgument<T>> {
		final ExecutionResult<ExecutionClosureArgument<T>> result = new ExecutionResult<>();

		MockClosure() {
			super(new Object());
		}

		protected R doCall(T t) {
			result.record(create(this, t));
			return null;
		}
	}

	public static <R, T, U> MockBiClosure<R, T, U> mockClosure(Class<T> firstArgumentType, Class<U> secondArgumentType) {
		return new MockBiClosure<R, T, U>();
	}

	public static final class MockBiClosure<R, T, U> extends groovy.lang.Closure<R> implements HasExecutionResult<ExecutionClosureBiArguments<T, U>> {
		final ExecutionResult<ExecutionClosureBiArguments<T, U>> result = new ExecutionResult<>();

		MockBiClosure() {
			super(new Object());
		}

		protected R doCall(T t, U u) {
			result.record(create(this, t, u));
			return null;
		}
	}

	/**
	 * Returns a closure that do something meaningless.
	 * All instance created are equal to each other.
	 *
	 * @return a closure that does something meaningless, never null.
	 */
	public static <R, T> Closure<R> doSomething(Class<T> firstArgumentType) {
		return new DoSomethingClosure<>();
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class DoSomethingClosure<R, T> extends Closure<R> {
		DoSomethingClosure() {
			super(new Object());
		}

		protected R doCall(T t) {
			return null;
		}

		@Override
		public String toString() {
			return "doSomething()";
		}
	}
}
