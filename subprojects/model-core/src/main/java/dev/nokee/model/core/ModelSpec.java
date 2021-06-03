package dev.nokee.model.core;

/**
 * Represent a predicate that is satisfied for a specific model projection type.
 *
 * @param <T>  the model projection type
 */
public interface ModelSpec<T> {
	boolean isSatisfiedBy(ModelNode node);

	default <S extends T> ModelSpec<S> and(ModelSpec<S> other) {
		return ModelSpecs.and(this, other);
	}
}
