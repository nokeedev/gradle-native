package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelPredicate;
import dev.nokee.model.internal.ModelSpecs;

/**
 * Represent a predicate that is satisfied for a specific model projection type.
 *
 * @param <T>  the model projection type
 */
public interface ModelSpec<T> extends ModelPredicate {
	Class<T> getProjectionType();

	default <S extends T> ModelSpec<S> and(ModelSpec<S> other) {
		return ModelSpecs.and(this, other);
	}

	default ModelSpec<T> or(ModelSpec<? super T> other) {
		return ModelSpecs.or(this, other);
	}
}
