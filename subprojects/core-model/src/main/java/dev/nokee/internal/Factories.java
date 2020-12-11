package dev.nokee.internal;

public final class Factories {
	private Factories() {}

	public static <T> Factory<T> alwaysThrow() {
		return AlwaysThrowFactory.ALWAYS_THROW.withNarrowedType();
	}

	private enum AlwaysThrowFactory implements Factory<Object> {
		ALWAYS_THROW {
			@Override
			public Object create() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				return "Factories.alwaysThrow()";
			}
		};

		@SuppressWarnings("unchecked") // safe contravariant cast
		<T> Factory<T> withNarrowedType() {
			return (Factory<T>) this;
		}
	}
}
