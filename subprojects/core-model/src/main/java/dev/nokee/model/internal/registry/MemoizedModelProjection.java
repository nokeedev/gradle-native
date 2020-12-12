package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

/**
 * A memoized projection that wraps a delegate projection.
 */
// TODO: consider creating memoised and Unmanaged creating projection together
@EqualsAndHashCode
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
