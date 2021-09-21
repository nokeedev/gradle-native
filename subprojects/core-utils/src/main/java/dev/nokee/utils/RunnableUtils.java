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
package dev.nokee.utils;

import lombok.EqualsAndHashCode;

/**
 * Utilities for {@link Runnable} instances.
 */
public final class RunnableUtils {
	private RunnableUtils() {}

	/**
	 * Returns a {@link Runnable} instance that will only run the specified runnable once.
	 * Subsequent invocation will return without running the specified runnable.
	 *
	 * @param delegate a runnable to run only once.
	 * @return a {@link Runnable} instance that run the specified runnable once, never null.
	 */
	public static Runnable onlyOnce(Runnable delegate) {
		return new OnlyOnceRunnable(delegate);
	}

	@EqualsAndHashCode
	private static final class OnlyOnceRunnable implements Runnable {
		private final Runnable delegate;
		@EqualsAndHashCode.Exclude private boolean ranOnce = false;

		private OnlyOnceRunnable(Runnable delegate) {
			this.delegate = delegate;
		}

		@Override
		public void run() {
			if (!ranOnce) {
				delegate.run();
				ranOnce = true;
			}
		}

		@Override
		public String toString() {
			return "RunnableUtils.onlyOnce(" + delegate + ")";
		}
	}
}
