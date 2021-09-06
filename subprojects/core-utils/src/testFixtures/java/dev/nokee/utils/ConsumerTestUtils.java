package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.gradle.api.Action;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;
import static java.util.Objects.requireNonNull;

public final class ConsumerTestUtils {
	/**
	 * Returns a consumer that do something meaningless.
	 * All instance created are equal to each other.
	 * <p>
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 *
	 * @return a consumer that does something meaningless, never null.
	 */
	public static <T> Consumer<T> aConsumer() {
		return new AConsumer<>();
	}

	/** @see #aConsumer() */
	@EqualsAndHashCode
	private static final class AConsumer<T> implements Consumer<T> {
		@Override
		public void accept(T t) {
			// doing something meaningless
		}

		@Override
		public String toString() {
			return "aConsumer()";
		}
	}

	/**
	 * Returns a consumer that do something meaningless different than {@link #aConsumer()}.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link #aConsumer()}?
	 * Because the implementation here convey that it's some work that is different than its counterpart.
	 *
	 * @return a consumer that does something else meaningless than {@link #aConsumer()}, never null.
	 */
	public static <T> Consumer<T> anotherConsumer() {
		return new AnotherConsumer<>(null);
	}

	@EqualsAndHashCode
	private static final class AnotherConsumer<T> implements Consumer<T> {
		@Nullable
		private final Object what;

		public AnotherConsumer(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public void accept(T node) {
			// doing something else meaningless
		}

		@Override
		public String toString() {
			return "anotherConsumer(" + (what == null ? "" : what) + ")";
		}
	}

	/**
	 * Returns a consumer that do something meaningless.
	 * All instance created are equal to each other.
	 * <p>
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 *
	 * @return a consumer that does something meaningless, never null.
	 */
	public static <T, U> BiConsumer<T, U> aBiConsumer() {
		return new ABiConsumer<>();
	}

	/** @see #aBiConsumer() */
	@EqualsAndHashCode
	private static final class ABiConsumer<T, U> implements BiConsumer<T, U> {
		@Override
		public void accept(T t, U u) {
			// doing something meaningless
		}

		@Override
		public String toString() {
			return "aBiConsumer()";
		}
	}

	/**
	 * Returns a consumer that do something meaningless different than {@link #aBiConsumer()}.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link #aBiConsumer()}?
	 * Because the implementation here convey that it's some work that is different than its counterpart.
	 *
	 * @return a consumer that does something else meaningless than {@link #aBiConsumer()}, never null.
	 */
	public static <T, U> BiConsumer<T, U> anotherBiConsumer() {
		return new AnotherBiConsumer<>(null);
	}

	@EqualsAndHashCode
	private static final class AnotherBiConsumer<T, U> implements BiConsumer<T, U> {
		@Nullable
		private final Object what;

		public AnotherBiConsumer(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public void accept(T t, U u) {
			// doing something else meaningless
		}

		@Override
		public String toString() {
			return "anotherBiConsumer(" + (what == null ? "" : what) + ")";
		}
	}

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
