package dev.nokee.utils;

import org.gradle.api.specs.Spec;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;

public final class SpecTestUtils {
	public static <T> MockSpec<T> mockSpec() {
		return new MockSpec<>();
	}

	public static <T> MockSpec<T> mockSpec(Class<T> tClass) {
		return new MockSpec<>();
	}

	/**
	 * @see #mockSpec()
	 * @see #mockSpec(Class)
	 */
	public static final class MockSpec<T> implements Spec<T>, HasExecutionResult<ExecutionArgument<T>> {
		final ExecutionResult<ExecutionArgument<T>> result = new ExecutionResult<>();
		private final Queue<Boolean> returnValues = new ArrayDeque<>();

		@Override
		public boolean isSatisfiedBy(T t) {
			result.record(create(this, t));
			return Optional.ofNullable(returnValues.poll()).orElse(false);
		}

		@SuppressWarnings("varargs")
		public MockSpec<T> thenReturn(boolean value, boolean... values) {
			returnValues.add(value);
			for (boolean v : values) {
				returnValues.add(v);
			}
			return this;
		}
	}
}
