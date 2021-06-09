package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.BuildVariant;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;

import java.util.List;

public interface BuildVariantInternal extends BuildVariant {
	List<Coordinate<?>> getDimensions();

	<T> T getAxisValue(CoordinateAxis<T> type);

	boolean hasAxisValue(CoordinateAxis<?> type);

	BuildVariantInternal withoutDimension(CoordinateAxis<?> type);
}
