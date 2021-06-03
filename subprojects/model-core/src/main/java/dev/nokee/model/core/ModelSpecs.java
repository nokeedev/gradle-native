package dev.nokee.model.core;

import lombok.EqualsAndHashCode;

final class ModelSpecs {
	public static <S> ModelSpec<S> and(ModelSpec<? super S> first, ModelSpec<S> second) {
		return new AndModelSpec<>(first, second);
	}

	/** @see #and(ModelSpec, ModelSpec) */
	@EqualsAndHashCode
	private static final class AndModelSpec<T> implements ModelSpec<T> {
		private final ModelSpec<? super T> first;
		private final ModelSpec<T> second;

		private AndModelSpec(ModelSpec<? super T> first, ModelSpec<T> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return first.isSatisfiedBy(node) && second.isSatisfiedBy(node);
		}

		@Override
		public String toString() {
			return "ModelSpecs.and(" + first + ", " + second + ")";
		}
	}

	public static <T> ModelSpec<T> ofType(Class<T> type) {
		return new OfTypeSpec<>(type);
	}

	/** @see #ofType(Class) */
	private static final class OfTypeSpec<T> implements ModelSpec<T> {
		private final Class<T> type;

		OfTypeSpec(Class<T> type) {
			this.type = type;
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return node.canBeViewedAs(type);
		}

		@Override
		public String toString() {
			return "ModelSpecs.ofType(" + type + ")";
		}
	}
}
