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

import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;

import javax.annotation.Nullable;

public final class ClosureTestUtils {
	/**
	 * Returns a closure that do something meaningless.
	 * All instance created are equal to each other.
	 *
	 * @return a closure that does something meaningless, never null.
	 */
	public static <R, T> Closure<R> doSomething(Class<T> firstArgumentType) {
		return new DoSomethingClosure<>();
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class DoSomethingClosure<R, T> extends Closure<R> {
		DoSomethingClosure() {
			super(new Object());
		}

		protected R doCall(T t) {
			return null;
		}

		@Override
		public String toString() {
			return "doSomething()";
		}
	}

	public static <R, T> Closure<R> doSomethingElse(Class<T> firstArgumentType) {
		return new DoSomethingElseClosure<>(null);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class DoSomethingElseClosure<R, T> extends Closure<R> {
		@Nullable
		private final Object what;

		public DoSomethingElseClosure(@Nullable Object what) {
			super(new Object());
			this.what = what;
		}

		protected R doCall(T t) {
			return null;
		}

		@Override
		public String toString() {
			return "doSomethingElse(" + (what == null ? "" : what) + ")";
		}
	}

	public static <T> Closure<Void> adapt(Action<? super T> action) {
		return new ActionClosure<>(action);
	}

	private static final class ActionClosure<T> extends Closure<Void> {
		private final Action<? super T> action;

		public ActionClosure(Action<? super T> action) {
			super(new Object());
			this.action = action;
		}

		public Void doCall(T t) {
			action.execute(t);
			return null;
		}
	}
}
