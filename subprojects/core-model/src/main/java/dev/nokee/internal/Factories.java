package dev.nokee.internal;

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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

	public static <T> Factory<T> constant(T value) {
		return new ConstantFactory<>(value);
	}

	@EqualsAndHashCode
	private static final class ConstantFactory<T> implements Factory<T> {
		private final T value;

		public ConstantFactory(T value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public T create() {
			return value;
		}

		@Override
		public String toString() {
			return "Factories.constant(" + value + ")";
		}
	}

	public static <T> Factory<T> memoize(Factory<T> factory) {
		return new MemoizeFactory<>(factory);
	}

	@EqualsAndHashCode
	private static final class MemoizeFactory<T> implements Factory<T> {
		private final Factory<T> delegate;
		@EqualsAndHashCode.Exclude private boolean initialized = false;
		@EqualsAndHashCode.Exclude @Nullable private T value;

		public MemoizeFactory(Factory<T> delegate) {
			this.delegate = Objects.requireNonNull(delegate);
		}

		@Override
		public T create() {
			if (!initialized) {
				value = delegate.create();
				initialized = true;
			}
			return value;
		}

		@Override
		public String toString() {
			return "Factories.memoize(" + delegate + ")";
		}
	}

	public static <T, R> Factory<R> compose(Factory<T> factory, Function<? super T, ? extends R> function) {
		return new ComposeFactory<>(factory, function);
	}

	@EqualsAndHashCode
	private static final class ComposeFactory<T, R> implements Factory<R> {
		private final Factory<T> factory;
		private final Function<? super T, ? extends R> function;

		public ComposeFactory(Factory<T> factory, Function<? super T, ? extends R> function) {
			this.factory = Objects.requireNonNull(factory);
			this.function = Objects.requireNonNull(function);
		}

		@Override
		public R create() {
			return function.apply(factory.create());
		}

		@Override
		public String toString() {
			return "Factories.compose(" + factory + ", " + function + ")";
		}
	}

	public static <T> Supplier<T> asSupplier(Factory<T> factory) {
		return new FactoryAsSupplier<>(factory);
	}

	@EqualsAndHashCode
	private static final class FactoryAsSupplier<T> implements Supplier<T> {
		private final Factory<T> factory;

		public FactoryAsSupplier(Factory<T> factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		@Override
		public T get() {
			return factory.create();
		}

		@Override
		public String toString() {
			return "Factories.asSupplier(" + factory + ")";
		}
	}
}
