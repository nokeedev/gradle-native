package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;

public final class MemoizedModelProjection implements ModelProjection {
	private final ModelProjection delegate;
	private Object value;

	public MemoizedModelProjection(ModelProjection delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return delegate.canBeViewedAs(type);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		if (value == null) {
			value = delegate.get(type);
		}
		return type.getConcreteType().cast(value);
	}
}
