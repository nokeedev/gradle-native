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
