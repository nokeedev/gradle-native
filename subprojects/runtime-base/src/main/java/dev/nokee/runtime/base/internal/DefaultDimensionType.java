package dev.nokee.runtime.base.internal;

import lombok.Value;

@Value
public class DefaultDimensionType<T extends Dimension> implements DimensionType<T> {
	Class<T> type;
}
