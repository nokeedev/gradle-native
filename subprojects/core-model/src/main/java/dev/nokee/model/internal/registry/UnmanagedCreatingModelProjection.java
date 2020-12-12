package dev.nokee.model.internal.registry;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * A projection that uses a factory to create the view instance.
 * A new view instance will be created on each query.
 * Users should use {@link MemoizedModelProjection} together with this projection to lock on the first instance create.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode
public final class UnmanagedCreatingModelProjection<M> implements ModelProjection {
	private final ModelType<M> type;
	private final Factory<M> factory;

	public UnmanagedCreatingModelProjection(ModelType<M> type, Factory<M> factory) {
		this.type = Objects.requireNonNull(type);
		this.factory = Objects.requireNonNull(factory);
	}

	public static <M> ModelProjection of(ModelType<M> type, Factory<M> factory) {
		return new UnmanagedCreatingModelProjection<>(type, factory);
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return type.isAssignableFrom(this.type);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(factory.create());
	}

	@Override
	public String toString() {
		return "UnmanagedCreatingModelProjection.of(" + type + ", " + factory + ")";
	}
}
