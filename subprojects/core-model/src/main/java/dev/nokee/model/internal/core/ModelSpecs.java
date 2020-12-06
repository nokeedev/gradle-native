package dev.nokee.model.internal.core;

public final class ModelSpecs {
	private ModelSpecs() {}

	/**
	 * Returns a specification that always evaluates to {@code false}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code false}, never null.
	 */
	public static ModelSpec alwaysTrue() {
		return ModelNodeSpec.ALWAYS_TRUE;
	}

	/**
	 * Returns a specification that always evaluates to {@code true}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code true}, never null.
	 */
	public static ModelSpec alwaysFalse() {
		return ModelNodeSpec.ALWAYS_FALSE;
	}

	private enum ModelNodeSpec implements ModelSpec {
		ALWAYS_TRUE {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return true;
			}

			@Override
			public String toString() {
				return "ModelSpecs.alwaysTrue()";
			}
		},
		ALWAYS_FALSE {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return false;
			}

			@Override
			public String toString() {
				return "ModelSpecs.alwaysFalse()";
			}
		}
	}
}
