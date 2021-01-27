package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.specs.Specs;

import java.util.Optional;

public final class SpecUtils {
	private SpecUtils() {}

	public static <T> Spec<T> byType(Class<? extends T> type) {
		return new ByTypeSpec<>(type);
	}

	public static <T> Optional<Class<? extends T>> getTypeFiltered(org.gradle.api.specs.Spec<T> spec) {
		if (spec instanceof ByTypeSpec) {
			return Optional.of(((ByTypeSpec<T>) spec).getType());
		}
		return Optional.empty();
	}

	@EqualsAndHashCode
	private static class ByTypeSpec<T> implements Spec<T> {
		private final Class<? extends T> type;

		public ByTypeSpec(Class<? extends T> type) {
			this.type = type;
		}

		public Class<? extends T> getType() {
			return type;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return type.isInstance(t);
		}

		@Override
		public String toString() {
			return "SpecUtils.byType(" + type.getCanonicalName() + ")";
		}
	}

	/**
	 * Returns a specification that always evaluates to true.
	 *
	 * @return a {@link Spec} that always evaluates to true, never null.
	 */
	public static <T> Spec<T> satisfyAll() {
		return ObjectSpec.SATISFY_ALL.withNarrowedType();
	}

	/**
	 * Returns a specification that always evaluates to false.
	 *
	 * @return a {@link Spec} that always evaluates to false, never null.
	 */
	public static <T> Spec<T> satisfyNone() {
		return ObjectSpec.SATISFY_NONE.withNarrowedType();
	}

	private enum ObjectSpec implements Spec<Object> {
		SATISFY_ALL {
			@Override
			public boolean isSatisfiedBy(Object element) {
				return true;
			}

			@Override
			public String toString() {
				return "SpecUtils.satisfyAll()";
			}
		},
		SATISFY_NONE {
			@Override
			public boolean isSatisfiedBy(Object element) {
				return false;
			}

			@Override
			public String toString() {
				return "SpecUtils.satisfyNone()";
			}
		};

		@SuppressWarnings("unchecked") // safe contravariant cast
		<T> Spec<T> withNarrowedType() {
			return (Spec<T>) this;
		}
	}

	private static boolean isSatisfyAll(org.gradle.api.specs.Spec<?> spec) {
		return spec == ObjectSpec.SATISFY_ALL || spec == Specs.SATISFIES_ALL;
	}

	private static boolean isSatisfyNone(org.gradle.api.specs.Spec<?> spec) {
		return spec == ObjectSpec.SATISFY_NONE || spec == Specs.SATISFIES_NONE;
	}

	public static <T> Spec<T> ofSpec(org.gradle.api.specs.Spec<? super T> spec) {
		return Spec.of(spec);
	}

	@FunctionalInterface
	public interface Spec<T> extends org.gradle.api.specs.Spec<T> {
		static <T> Spec<T> of(org.gradle.api.specs.Spec<? super T> spec) {
			if (isSatisfyAll(spec)) {
				return satisfyAll();
			} else if (isSatisfyNone(spec)) {
				return satisfyNone();
			} else if (spec instanceof Spec) {
				@SuppressWarnings("unchecked")
				val result = (Spec<T>) spec;
				return result;
			}
			return new WrappedSpec<>(spec);
		}
	}

	/** @see Spec#of(org.gradle.api.specs.Spec) */
	@EqualsAndHashCode
	private static final class WrappedSpec<T> implements Spec<T> {
		private final org.gradle.api.specs.Spec<? super T> sourceSpec;

		private WrappedSpec(org.gradle.api.specs.Spec<? super T> sourceSpec) {
			this.sourceSpec = sourceSpec;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return sourceSpec.isSatisfiedBy(t);
		}

		@Override
		public String toString() {
			return "SpecUtils.Spec.of(" + sourceSpec + ")";
		}
	}
}
