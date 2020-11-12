package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Defer realization in the context of Gradle using various tricks.
 */
public final class DeferUtils {
	private DeferUtils() {}

	/**
	 * Returns an {@link Object} instance that realize the specified supplier only when calling Object#toString().
	 * @param supplier a supplier of String to realize during Object#toString()
	 * @return an {@link Object} instance realizing the specified supplier when calling Object#toString(), never null.
	 */
	public static Object asToStringObject(Supplier<String> supplier) {
		return new ToStringSupplierObject(supplier);
	}

	@EqualsAndHashCode
	private static final class ToStringSupplierObject {
		private final Supplier<String> supplier;

		private ToStringSupplierObject(Supplier<String> supplier) {
			this.supplier = requireNonNull(supplier);
		}

		@Override
		public String toString() {
			return supplier.get();
		}
	}

	/**
	 * Returns a {@link Provider} instance that runs the specified {@link Runnable} when resolved.
	 * The provider always run the {@link Runnable} and return an empty list.
	 *
	 * @param delegate a runnable to run upon resolving the provider.
	 * @param <S> the element type of returned list
	 * @return a provider that runs the specified runnable and return a empty list, never null.
	 */
	public static <S> Provider<List<S>> executes(Runnable delegate) {
		return ProviderUtils.supplied(new DeferRunnableViaEmptyListReturningCallable<>(delegate));
	}

	private static final class DeferRunnableViaEmptyListReturningCallable<T> implements Callable<List<T>> {
		private final Runnable delegate;

		public DeferRunnableViaEmptyListReturningCallable(Runnable delegate) {
			this.delegate = delegate;
		}

		@Override
		public List<T> call() throws Exception {
			delegate.run();
			return Collections.emptyList();
		}
	}
}
