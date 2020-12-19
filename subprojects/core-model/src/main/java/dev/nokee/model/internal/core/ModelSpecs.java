package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;

import java.util.function.Predicate;

public final class ModelSpecs {
	private ModelSpecs() {}

	/**
	 * Returns a specification that always evaluates to {@code false}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code false}, never null.
	 */
	public static ModelSpec satisfyAll() {
		return ModelNodeSpec.SATISFY_ALL;
	}

	/**
	 * Returns a specification that always evaluates to {@code true}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code true}, never null.
	 */
	public static ModelSpec satisfyNone() {
		return ModelNodeSpec.SATISFY_NONE;
	}

	private enum ModelNodeSpec implements ModelSpec {
		SATISFY_ALL {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return true;
			}

			@Override
			public String toString() {
				return "ModelSpecs.satisfyAll()";
			}
		},
		SATISFY_NONE {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return false;
			}

			@Override
			public String toString() {
				return "ModelSpecs.satisfyNone()";
			}
		}
	}

	/**
	 * Returns a predicate adapted as a model spec.
	 *
	 * @param predicate  the predicate to adapt
	 * @return a {@link ModelSpec} delegating to the specified predicate, never null.
	 */
	public static ModelSpec of(Predicate<? super ModelNode> predicate) {
		return new OfPredicateModelSpec(predicate);
	}

	@EqualsAndHashCode
	private static final class OfPredicateModelSpec implements ModelSpec {
		private final Predicate<? super ModelNode> predicate;

		public OfPredicateModelSpec(Predicate<? super ModelNode> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return predicate.test(node);
		}

		@Override
		public String toString() {
			return "ModelSpecs.of(" + predicate + ")";
		}
	}
}
