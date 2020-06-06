package dev.nokee.platform.base.internal;

import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;

import java.util.List;

public interface BuildVariant {
	List<Dimension> getDimensions();

	<T extends Dimension> T getAxisValue(DimensionType<T> type);

	boolean hasAxisValue(DimensionType<? extends Dimension> type);
}
