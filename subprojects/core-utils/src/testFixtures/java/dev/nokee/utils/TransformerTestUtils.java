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
import org.gradle.api.Transformer;

import javax.annotation.Nullable;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;

public final class TransformerTestUtils {

	public static <OUT, IN> TransformerUtils.Transformer<OUT, IN> aTransformer() {
		return new ATransformer<>();
	}

	/** @see #aTransformer() */
	@EqualsAndHashCode
	private static final class ATransformer<OUT, IN> implements TransformerUtils.Transformer<OUT, IN> {
		@Override
		@SuppressWarnings("unchecked")
		public OUT transform(IN t) {
			// do not depend on the result of this transformer
			try {
				return (OUT) t; // try a sensible return value
			} catch (ClassCastException ex) {
				return null; // returning null breaks the Gradle API contract so we can assume the transformer won't be executed
			}
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

	public static <OUT, IN> MockTransformer<OUT, IN> mockTransformer() {
		return new MockTransformer<>();
	}

	public static final class MockTransformer<OUT, IN> implements Transformer<OUT, IN>, HasExecutionResult<ExecutionArgument<IN>> {
		final ExecutionResult<ExecutionArgument<IN>> result = new ExecutionResult<>();
		private Transformer<OUT, IN> answer = it -> null; // force return null, but it's almost certainly wrong

		@Override
		public OUT transform(IN t) {
			result.record(create(this, t));
			return answer.transform(t);
		}

		public MockTransformer<OUT, IN> whenCalled(Transformer<OUT, IN> answer) {
			this.answer = answer;
			return this;
		}
	}
}
