package dev.nokee.utils;

import lombok.EqualsAndHashCode;

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
}
