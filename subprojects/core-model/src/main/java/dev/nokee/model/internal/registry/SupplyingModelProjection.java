package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A projection that uses a factory to create the view instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode(callSuper = true)
public final class SupplyingModelProjection<M> extends TypeCompatibilityModelProjectionSupport<M> {
	private final Supplier<M> supplier;

	public SupplyingModelProjection(ModelType<M> type, Supplier<M> supplier) {
		super(type);
		this.supplier = Objects.requireNonNull(supplier);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(supplier.get());
	}

	@Override
	public String toString() {
		return "SupplyingModelProjection.of(" + getType() + ", " + supplier + ")";
	}
}
