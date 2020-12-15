package dev.nokee.model.internal.registry;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * A projection that uses a factory to create the view instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode(callSuper = true)
public final class SupplyingModelProjection<M> extends TypeCompatibilityModelProjectionSupport<M> {
	private final Factory<M> factory;

	public SupplyingModelProjection(ModelType<M> type, Factory<M> factory) {
		super(type);
		this.factory = Objects.requireNonNull(factory);
	}

	public static <M> ModelProjection of(ModelType<M> type, Factory<M> factory) {
		return new SupplyingModelProjection<>(type, factory);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(factory.create());
	}

	@Override
	public String toString() {
		return "SupplyingModelProjection.of(" + getType() + ", " + factory + ")";
	}
}
