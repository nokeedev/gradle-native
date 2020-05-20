package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class DefaultDimensionType implements DimensionType {
	Class<? extends Dimension> type;
}
