package dev.nokee.runtime.base.internal;

import lombok.Value;

@Value
public class DefaultDimensionType implements DimensionType {
	Class<? extends Dimension> type;
}
