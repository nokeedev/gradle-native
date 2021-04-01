package dev.gradleplugins.grava.testing.util;

import dev.gradleplugins.grava.util.TransformerUtils;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

public final class TransformerTestUtils {

	public static <T> TransformerUtils.Transformer<T, T> aTransformer() {
		return new ATransformer<>();
	}

	/** @see #aTransformer() */
	@EqualsAndHashCode
	private static final class ATransformer<T> implements TransformerUtils.Transformer<T, T> {
		@Override
		public T transform(T t) {
			return t;
		}

		@Override
		public String toString() {
			return "aTransformer()";
		}
	}

	public static <T> TransformerUtils.Transformer<T, T> anotherTransformer() {
		return new AnotherTransformer<>(null);
	}

	public static <T> TransformerUtils.Transformer<T, T> anotherTransformer(Object what) {
		return new AnotherTransformer<>(what);
	}

	/** @see #anotherTransformer() */
	@EqualsAndHashCode
	private static final class AnotherTransformer<T> implements TransformerUtils.Transformer<T, T> {
		@Nullable private final Object what;

		public AnotherTransformer(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public T transform(T t) {
			return t;
		}

		@Override
		public String toString() {
			return "anotherTransformer(" + (what == null ? "" : what) + ")";
		}
	}
}
