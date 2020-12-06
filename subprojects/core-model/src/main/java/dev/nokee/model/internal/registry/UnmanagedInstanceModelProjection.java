package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;

import java.util.Objects;

/**
 * A projection always returning the same instance.
 *
 * @param <M>  the type of the projection
 */
public final class UnmanagedInstanceModelProjection<M> implements ModelProjection {
	private final M instance;

	private UnmanagedInstanceModelProjection(M instance) {
		this.instance = instance;
	}

	public static <M> ModelProjection of(M instance) {
		Objects.requireNonNull(instance);
		return new UnmanagedInstanceModelProjection<>(instance);
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return true;
	}

	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(instance);
	}
}
