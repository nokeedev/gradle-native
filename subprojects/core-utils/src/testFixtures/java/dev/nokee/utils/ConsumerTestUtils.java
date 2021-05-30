package dev.nokee.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;

public final class ConsumerTestUtils {
	public static <T> MockConsumer<T> mockConsumer() {
		return new MockConsumer<>();
	}

	public static <T> MockConsumer<T> mockConsumer(Class<T> tClass) {
		return new MockConsumer<>();
	}

	public static <T, U> MockBiConsumer<T, U> mockConsumer(Class<T> tClass, Class<U> uClass) {
		return new MockBiConsumer<>();
	}

	/**
	 * @see #mockConsumer()
	 * @see #mockConsumer(Class)
	 */
	public static final class MockConsumer<T> implements Consumer<T>, HasExecutionResult<ExecutionArgument<T>> {
		final ExecutionResult<ExecutionArgument<T>> result = new ExecutionResult<>();

		@Override
		public void accept(T t) {
			result.record(create(this, t));
		}
	}

	public static <T, U> MockBiConsumer<T, U> mockBiConsumer() {
		return new MockBiConsumer<>();
	}

	/**
	 * @see #mockBiConsumer()
	 * @see #mockConsumer(Class, Class)
	 */
	public static final class MockBiConsumer<T, U> implements BiConsumer<T, U>, HasExecutionResult<ExecutionBiArguments<T, U>> {
		final ExecutionResult<ExecutionBiArguments<T, U>> result = new ExecutionResult<>();

		@Override
		public void accept(T t, U u) {
			result.record(create(this, t, u));
		}
	}
}
