package dev.gradleplugins.grava.util;

import dev.nokee.utils.Cast;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.specs.Specs;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class SpecUtils {
	private SpecUtils() {}

	public static <A, B> Spec<A> compose(org.gradle.api.specs.Spec<? super B> spec, Transformer<? extends B, ? super A> transformer) {
		if (isSatisfyAll(spec)) {
			return satisfyAll();
		} else if (isSatisfyNone(spec)) {
			return satisfyNone();
		} else if (TransformerUtils.isNoOpTransformer(transformer)) {
			return Cast.uncheckedCast("no op transformer implies the output type is the same", spec);
		}
		return new ComposeSpec<>(spec, transformer);
	}

	@EqualsAndHashCode
	private static final class ComposeSpec<A, B> implements Spec<A> {
		private final org.gradle.api.specs.Spec<? super B> spec;
		private final Transformer<? extends B, ? super A> transformer;

		public ComposeSpec(org.gradle.api.specs.Spec<? super B> spec, Transformer<? extends B, ? super A> transformer) {
			this.spec = requireNonNull(spec);
			this.transformer = requireNonNull(transformer);
		}

		@Override
		public boolean isSatisfiedBy(A in) {
			return spec.isSatisfiedBy(transformer.transform(in));
		}

		@Override
		public String toString() {
			return "SpecUtils.compose(" + spec + ", " + transformer + ")";
		}
	}

	public static Spec<Class<?>> subtypeOf(Class<?> clazz) {
		if (clazz.equals(Object.class)) {
			return satisfyAll();
		}
		return new SubtypeOfSpec(clazz);
	}

	/** @see #subtypeOf(Class) */
	@EqualsAndHashCode
	private static final class SubtypeOfSpec implements Spec<Class<?>> {
		private final Class<?> type;

		SubtypeOfSpec(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean isSatisfiedBy(Class<?> clazz) {
			return type.isAssignableFrom(clazz);
		}

		@Override
		public String toString() {
			return "SpecUtils.subtypeOf(" + type + ")";
		}
	}

	public static Spec<Object> instanceOf(Class<?> clazz) {
		if (Object.class.equals(clazz)) {
			return satisfyAll();
		}
		return new InstanceOfSpec(clazz);
	}

	public static <T> Spec<Object> instanceOf(Class<T> clazz, org.gradle.api.specs.Spec<? super T> andSpec) {
		requireNonNull(andSpec);
		if (Object.class.equals(clazz)) {
			return satisfyAll();
		}
		return and(new InstanceOfSpec(clazz), (org.gradle.api.specs.Spec<? super Object>) andSpec);
	}

	/** @see #instanceOf(Class) */
	@EqualsAndHashCode
	private static final class InstanceOfSpec implements Spec<Object> {
		private final Class<?> clazz;

		public InstanceOfSpec(Class<?> clazz) {
			this.clazz = requireNonNull(clazz);
		}

		@Override
		public boolean isSatisfiedBy(Object in) {
			return clazz.isInstance(in);
		}

		@Override
		public String toString() {
			return "SpecUtils.instanceOf(" + clazz + ")";
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

	/**
	 * @see #satisfyAll()
	 * @see #satisfyNone()
	 */
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

	/**
	 * Returns a specification that negate the specified specification.
	 *
	 * @param spec  a specification to negate, must not be null
	 * @param <T>  the specification input type
	 * @return a {@link Spec} that negate the specified specification, never null
	 */
	public static <T> Spec<T> negate(org.gradle.api.specs.Spec<? super T> spec) {
		if (isSatisfyAll(spec)) {
			return satisfyNone();
		} else if (isSatisfyNone(spec)) {
			return satisfyAll();
		} else if (spec instanceof NegateSpec) {
			@SuppressWarnings("unchecked")
			NegateSpec<? super T> negateSpec = (NegateSpec<? super T>)spec;
			return Spec.of(negateSpec.getSourceSpec());
		} else {
			return new NegateSpec<>(spec);
		}
	}

	/** @see #negate(org.gradle.api.specs.Spec) */
	@EqualsAndHashCode
	private static final class NegateSpec<T> implements Spec<T> {
		private final org.gradle.api.specs.Spec<? super T> sourceSpec;

		public NegateSpec(org.gradle.api.specs.Spec<? super T> spec) {
			this.sourceSpec = requireNonNull(spec);
		}

		public org.gradle.api.specs.Spec<? super T> getSourceSpec() {
			return sourceSpec;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return !sourceSpec.isSatisfiedBy(t);
		}

		@Override
		public String toString() {
			return "SpecUtils.negate(" + sourceSpec + ")";
		}
	}

	public static <T> Spec<T> or(org.gradle.api.specs.Spec<? super T> first, org.gradle.api.specs.Spec<? super T> second) {
		if (isSatisfyAll(first) || isSatisfyAll(second)) {
			return satisfyAll();
		}

		if (isSatisfyNone(first)) {
			return Spec.of(second);
		}

		if (isSatisfyNone(second)) {
			return Spec.of(first);
		}

		if (first.equals(second)) {
			return Spec.of(first);
		}

		return new OrSpec<>(first, second);
	}

	/** @see #or(org.gradle.api.specs.Spec, org.gradle.api.specs.Spec) */
	@EqualsAndHashCode
	private static final class OrSpec<T> implements Spec<T> {
		private final Set<org.gradle.api.specs.Spec<? super T>> specs = new LinkedHashSet<>();

		public OrSpec(org.gradle.api.specs.Spec<? super T> first, org.gradle.api.specs.Spec<? super T> second) {
			specs.add(requireNonNull(first));
			specs.add(requireNonNull(second));
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			for (org.gradle.api.specs.Spec<? super T> spec : specs) {
				if (spec.isSatisfiedBy(t)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			val iter = specs.iterator();
			return "SpecUtils.or(" + iter.next() + ", " + iter.next() + ")";
		}
	}

	public static <T> Spec<T> and(org.gradle.api.specs.Spec<? super T> first, org.gradle.api.specs.Spec<? super T> second) {
		if (isSatisfyNone(first) || isSatisfyNone(second)) {
			return satisfyNone();
		}

		if (isSatisfyAll(first)) {
			return Spec.of(second);
		}

		if (isSatisfyAll(second)) {
			return Spec.of(first);
		}

		if (first.equals(second)) {
			return Spec.of(first);
		}

		return new AndSpec<>(first, second);
	}

	/** @see #and(org.gradle.api.specs.Spec, org.gradle.api.specs.Spec) */
	@EqualsAndHashCode
	private static final class AndSpec<T> implements Spec<T> {
		private final Set<org.gradle.api.specs.Spec<? super T>> specs = new LinkedHashSet<>();

		public AndSpec(org.gradle.api.specs.Spec<? super T> first, org.gradle.api.specs.Spec<? super T> second) {
			this.specs.add(requireNonNull(first));
			this.specs.add(requireNonNull(second));
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			for (org.gradle.api.specs.Spec<? super T> spec : specs) {
				if (!spec.isSatisfiedBy(t)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			val iter = specs.iterator();
			return "SpecUtils.and(" + iter.next() + ", " + iter.next() + ")";
		}
	}

	static boolean isSatisfyAll(org.gradle.api.specs.Spec<?> spec) {
		return spec == ObjectSpec.SATISFY_ALL || spec == Specs.SATISFIES_ALL;
	}

	static boolean isSatisfyNone(org.gradle.api.specs.Spec<?> spec) {
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

		default Spec<T> negate() {
			return SpecUtils.negate(this);
		}

		default Spec<T> or(org.gradle.api.specs.Spec<? super T> other) {
			return SpecUtils.or(this, other);
		}

		default Spec<T> and(org.gradle.api.specs.Spec<? super T> other) {
			return SpecUtils.and(this, other);
		}
	}

	/** @see Spec#of(org.gradle.api.specs.Spec) */
	@EqualsAndHashCode
	private static final class WrappedSpec<T> implements Spec<T> {
		private final org.gradle.api.specs.Spec<? super T> sourceSpec;

		private WrappedSpec(org.gradle.api.specs.Spec<? super T> sourceSpec) {
			this.sourceSpec = requireNonNull(sourceSpec);
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
