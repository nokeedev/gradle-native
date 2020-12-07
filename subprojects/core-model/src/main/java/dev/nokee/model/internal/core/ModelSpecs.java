package dev.nokee.model.internal.core;

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
}
