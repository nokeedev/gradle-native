package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode
public abstract class TypeCompatibilityModelProjectionSupport<M> implements ModelProjection {
	private final ModelType<M> type;

	protected TypeCompatibilityModelProjectionSupport(ModelType<M> type) {
		this.type = Objects.requireNonNull(type);
	}

	protected ModelType<M> getType() {
		return type;
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return type.isAssignableFrom(this.type);
	}

	@Override
	public Iterable<String> getTypeDescriptions() {
		return ImmutableList.of(description(type));
	}

	public static String description(ModelType<?> type) {
		if (!type.getSupertype().isPresent() && type.getInterfaces().isEmpty()) {
			return type.toString();
		}
		return type.toString() + " (or assignment compatible type thereof)";
	}
}
