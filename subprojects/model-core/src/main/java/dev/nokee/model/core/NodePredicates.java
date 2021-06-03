package dev.nokee.model.core;

public interface NodePredicates {
	static NodePredicate<Object> descendants() {
		return NodePredicate.descendants();
	}

	static <T> NodePredicate<T> descendants(ModelSpec<T> spec) {
		return NodePredicate.descendants(spec::isSatisfiedBy);
	}

	static NodePredicate<Object> directChildren() {
		return NodePredicate.directChildren();
	}

	static <T> NodePredicate<T> directChildren(ModelSpec<T> spec) {
		return NodePredicate.directChildren(spec::isSatisfiedBy);
	}

	static <T> ModelSpec<T> ofType(Class<T> type) {
		return ModelSpecs.ofType(type);
	}
}
