package dev.nokee.utils;

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Random;

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
}
