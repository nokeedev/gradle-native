/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
				throw new UnsupportedOperationException("This factory always throw an exception.");
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
