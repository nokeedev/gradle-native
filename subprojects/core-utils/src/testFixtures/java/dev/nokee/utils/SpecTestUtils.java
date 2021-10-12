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
import org.gradle.api.specs.Spec;

import javax.annotation.Nullable;
import java.util.Random;

import static dev.nokee.utils.ExecutionArgumentsFactory.create;
import static java.util.Objects.requireNonNull;

public class SpecTestUtils {
	/**
	 * Returns an spec that is satisfied in an undefined way.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link SpecUtils#satisfyAll()} or {@link SpecUtils#satisfyNone()}?
	 * Because the implementation here should not be considered a specific outcome but rather some specification that we don't really care for the purpose of the test.
	 *
	 * @return a specification that is satisfied in an undefined way, never null.
	 */
	public static <T> SpecUtils.Spec<T> aSpec() {
		return new ASpec<>();
	}

	@EqualsAndHashCode
	private static final class ASpec<T> implements SpecUtils.Spec<T> {
		@Override
		public boolean isSatisfiedBy(T t) {
			return new Random().nextBoolean();
		}

		@Override
		public String toString() {
			return "aSpec()";
		}
	}

	/**
	 * Returns a specification that is satisfied differently than {@link #aSpec()} and {@link #anotherSpec(Object)}.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link SpecUtils#satisfyAll()} or {@link SpecUtils#satisfyNone()}?
	 * Because the implementation here should not be considered a specific outcome but rather some specification that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #aSpec()}?
	 * Because the implementation here convey that it's some specification that is different than its counterpart.
	 * <p>
	 * Why not use {@link #anotherSpec(Object)}?
	 * Because the implementation here convey that it's some specification that is different but not specific on what is different.
	 *
	 * @return a specification that is satisfied in an undefined way different than {@link #aSpec()}, never null.
	 */
	public static <T> SpecUtils.Spec<T> anotherSpec() {
		return new AnotherSpec<>(null);
	}

	/**
	 * Returns an action that is satisfied in an undefined way distinguishable from {@link #aSpec()} and {@link #anotherSpec()}.
	 * All instance created with the same {@literal what} are equal to each other.
	 * <p>
	 * Why not use {@link SpecUtils#satisfyAll()} or {@link SpecUtils#satisfyNone()}?
	 * Because the implementation here should not be considered a specific outcome but rather some specification that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #aSpec()} or {@link #anotherSpec()} ()}?
	 * Because this implementation here convey "what" is different than its counterpart.
	 *
	 * @param what  the differentiator for the specification to be satisfied
	 * @return a specification that is satisfied in an undefined way different than {@link #aSpec()} and {@link #anotherSpec()}, never null.
	 */
	public static <T> SpecUtils.Spec<T> anotherSpec(Object what) {
		return new AnotherSpec<>(requireNonNull(what));
	}

	@EqualsAndHashCode
	private static final class AnotherSpec<T> implements SpecUtils.Spec<T> {
		@Nullable
		private final Object what;

		public AnotherSpec(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return new Random().nextBoolean();
		}

		@Override
		public String toString() {
			return "anotherSpec(" + (what == null ? "" : what) + ")";
		}
	}

	public static <T> MockSpec<T> mockSpec() {
		return new MockSpec<>();
	}

	public static final class MockSpec<T> implements Spec<T>, HasExecutionResult<ExecutionArgument<T>> {
		final ExecutionResult<ExecutionArgument<T>> result = new ExecutionResult<>();
		private Spec<T> answer = aSpec();

		@Override
		public boolean isSatisfiedBy(T t) {
			result.record(create(this, t));
			return answer.isSatisfiedBy(t);
		}

		public MockSpec<T> whenCalled(Spec<T> answer) {
			this.answer = answer;
			return this;
		}
	}
}
