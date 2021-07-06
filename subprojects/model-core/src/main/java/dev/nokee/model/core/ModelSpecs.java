package dev.nokee.model.core;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.function.Predicate;

final class ModelSpecs {
	public static <S> ModelSpec<S> and(ModelSpec<? super S> first, ModelSpec<S> second) {
		if (first.equals(second)) {
			return second;
		} else {
			return new AndModelSpec<>(first, second);
		}
	}

	/** @see #and(ModelSpec, ModelSpec) */
	@EqualsAndHashCode
	private static final class AndModelSpec<T> implements ModelSpec<T>, Predicate<ModelProjection> {
		private final ModelSpec<? super T> first;
		private final ModelSpec<T> second;

		private AndModelSpec(ModelSpec<? super T> first, ModelSpec<T> second) {
			Preconditions.checkArgument(first.getProjectionType().isAssignableFrom(second.getProjectionType()), "projection type '%s' needs to be the same or subtype of '%s'", second.getProjectionType().getCanonicalName(), first.getProjectionType().getCanonicalName());
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean isSatisfiedBy(ModelProjection subject) {
			return first.isSatisfiedBy(subject) && second.isSatisfiedBy(subject);
		}

		@Override
		public boolean test(ModelProjection subject) {
			return isSatisfiedBy(subject);
		}

		@Override
		public Class<T> getProjectionType() {
			return second.getProjectionType();
		}

		@Override
		public String toString() {
			return "ModelSpecs.and(" + first + ", " + second + ")";
		}
	}

	public static <T> ModelSpec<T> or(ModelSpec<T> first, ModelSpec<? super T> second) {
		if (first.equals(second)) {
			return first;
		}

		return new OrModelSpec<>(first, second);
	}

	/** @see #and(ModelSpec, ModelSpec) */
	@EqualsAndHashCode
	private static final class OrModelSpec<T> implements ModelSpec<T>, Predicate<ModelProjection> {
		private final ModelSpec<T> first;
		private final ModelSpec<? super T> second;

		private OrModelSpec(ModelSpec<T> first, ModelSpec<? super T> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean isSatisfiedBy(ModelProjection subject) {
			return first.isSatisfiedBy(subject) || second.isSatisfiedBy(subject);
		}

		@Override
		public boolean test(ModelProjection subject) {
			return isSatisfiedBy(subject);
		}

		@Override
		public Class<T> getProjectionType() {
			return first.getProjectionType();
		}

		@Override
		public String toString() {
			return "ModelSpecs.or(" + first + ", " + second + ")";
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
		public boolean isSatisfiedBy(ModelProjection subject) {
			return subject.canBeViewedAs(type);
		}

		@Override
		public Class<T> getProjectionType() {
			return type;
		}

		@Override
		public String toString() {
			return "ModelSpecs.ofType(" + type + ")";
		}
	}

	public static ModelSpec<Object> alwaysTrue() {
		return AlwaysTrueSpec.INSTANCE;
	}

	private enum AlwaysTrueSpec implements ModelSpec<Object> {
		INSTANCE;

		@Override
		public boolean isSatisfiedBy(ModelProjection node) {
			return true;
		}

		@Override
		public Class<Object> getProjectionType() {
			return Object.class;
		}

		@Override
		public String toString() {
			return ""; // empty string on purpose
		}
	};

	public static ModelSpec<Object> isSelf(ModelNode node) {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection subject) {
				return subject.getOwner().equals(node);
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}

	public static ModelSpec<Object> withAncestor(ModelNode node) {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection subject) {
				Optional<ModelNode> parent = subject.getOwner().getParent();
				while (parent.isPresent()) {
					if (parent.get().equals(node)) {
						return true;
					}
					parent = parent.get().getParent();
				}
				return false;
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}

	public static ModelSpec<Object> withParent(ModelNode node) {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection subject) {
				return subject.getOwner().getParent().map(it -> it.equals(node)).orElse(false);
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}
}
