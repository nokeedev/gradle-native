package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.core.CoordinateAxis;

import java.util.Comparator;

final class SingleVariantDimensionComparator<T> implements Comparator<VariantInternal> {
	private final CoordinateAxis<T> type;
	private final Comparator<? super T> delegate;

	protected SingleVariantDimensionComparator(CoordinateAxis<T> type, Comparator<? super T> delegate) {
		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public final int compare(VariantInternal lhs, VariantInternal rhs) {
		if (lhs.getBuildVariant().hasAxisValue(type) && rhs.getBuildVariant().hasAxisValue(type)) {
			return delegate.compare(lhs.getBuildVariant().getAxisValue(type), rhs.getBuildVariant().getAxisValue(type));
		} else if (lhs.getBuildVariant().hasAxisValue(type)) {
			return -1;
		} else if (rhs.getBuildVariant().hasAxisValue(type)) {
			return 1;
		}
		return 0;
	}
}
