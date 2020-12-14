package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * A projection always returning the same instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode(callSuper = false)
public final class UnmanagedInstanceModelProjection<M> extends TypeCompatibilityModelProjectionSupport<M> {
	private final M instance;

	private UnmanagedInstanceModelProjection(M instance) {
		super(ModelType.typeOf(instance));
		this.instance = Objects.requireNonNull(instance);
	}

	public static <M> ModelProjection of(M instance) {
		return new UnmanagedInstanceModelProjection<>(instance);
	}

	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(instance);
	}
}
