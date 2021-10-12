/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

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
