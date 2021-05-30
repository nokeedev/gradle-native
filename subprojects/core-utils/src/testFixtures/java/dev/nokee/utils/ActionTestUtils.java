package dev.nokee.utils;

import org.gradle.api.Action;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;

public final class ActionTestUtils {
	public static <T> MockAction<T> mockAction() {
		return new MockAction<>();
	}

	public static <T> MockAction<T> mockAction(Class<T> tClass) {
		return new MockAction<>();
	}

	public static final class MockAction<T> implements Action<T>, HasExecutionResult<ExecutionArgument<T>> {
		final ExecutionResult<ExecutionArgument<T>> result = new ExecutionResult<>();

		@Override
		public void execute(T t) {
			result.record(create(this, t));
		}
	}
}
