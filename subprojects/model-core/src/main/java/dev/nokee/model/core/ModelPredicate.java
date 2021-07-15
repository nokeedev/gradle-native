package dev.nokee.model.core;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface ModelPredicate extends Predicate<ModelProjection> {
	@Override
	default Predicate<ModelProjection> and(Predicate<? super ModelProjection> other) {
		if (Objects.equals(this, other)) {
			return this;
		}
		return Predicate.super.and(other);
	}

	@Override
	default Predicate<ModelProjection> or(Predicate<? super ModelProjection> other) {
		if (Objects.equals(this, other)) {
			return this;
		}
		return Predicate.super.or(other);
	}
}
