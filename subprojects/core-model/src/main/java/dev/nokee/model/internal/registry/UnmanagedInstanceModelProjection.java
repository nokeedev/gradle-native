package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * A projection always returning the same instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode
public final class UnmanagedInstanceModelProjection<M> implements ModelProjection {
	private final M instance;
	@EqualsAndHashCode.Exclude private final ModelType<M> type;

	private UnmanagedInstanceModelProjection(M instance) {
		this.instance = Objects.requireNonNull(instance);
		this.type = ModelType.typeOf(instance);
	}

	public static <M> ModelProjection of(M instance) {
		return new UnmanagedInstanceModelProjection<>(instance);
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return type.isAssignableFrom(this.type);
	}

	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(instance);
	}
}
