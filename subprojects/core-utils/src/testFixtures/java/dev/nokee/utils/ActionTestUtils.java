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
import org.gradle.api.Action;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class ActionTestUtils {
	/**
	 * Returns an action that do something meaningless.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link ActionUtils#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 *
	 * @return an action that does something meaningless, never null.
	 */
	public static <T> Action<T> doSomething() {
		return new DoSomethingAction<>();
	}

	@EqualsAndHashCode
	private static final class DoSomethingAction<T> implements ActionUtils.Action<T> {
		@Override
		public void execute(T t) {
			// doing something meaningless
		}

		@Override
		public String toString() {
			return "doSomething()";
		}
	}

	/**
	 * Returns an action that do something meaningless different than {@link #doSomething()} and {@link #doSomethingElse(Object)}.
	 * All instance created are equal to each other.
	 * <p>
	 * Why not use {@link ActionUtils#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #doSomething()}?
	 * Because the implementation here convey that it's some work that is different than its counterpart.
	 * <p>
	 * Why not use {@link #doSomethingElse(Object)}?
	 * Because the implementation here convey that it's some work that is different but not specific on what is different.
	 *
	 * @return an action that does something else meaningless than {@link #doSomething()}, never null.
	 */
	public static <T> Action<T> doSomethingElse() {
		return new DoSomethingElseAction<>(null);
	}

	/**
	 * Returns an action that do something meaningless distinguishable from {@link #doSomething()} and {@link #doSomethingElse()}.
	 * All instance created with the same {@literal what} are equal to each other.
	 * <p>
	 * Why not use {@link ActionUtils#doNothing()}?
	 * Because the implementation here should not be considered a no-op but rather some work that we don't really care for the purpose of the test.
	 * <p>
	 * Why not use {@link #doSomething()} or {@link #doSomethingElse()}?
	 * Because this implementation here convey "what" is different than its counterpart.
	 *
	 * @param what  the differentiator for the work to be done
	 * @return an action that does something else meaningless than {@link #doSomething()} and {@link #doSomethingElse()}, never null.
	 */
	public static <T> Action<T> doSomethingElse(Object what) {
		return new DoSomethingElseAction<>(requireNonNull(what));
	}

	@EqualsAndHashCode
	private static final class DoSomethingElseAction<T> implements ActionUtils.Action<T> {
		@Nullable
		private final Object what;

		public DoSomethingElseAction(@Nullable Object what) {
			this.what = what;
		}

		@Override
		public void execute(T node) {
			// doing something else meaningless
		}

		@Override
		public String toString() {
			return "doSomethingElse(" + (what == null ? "" : what) + ")";
		}
	}
}
