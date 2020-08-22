package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;

import java.util.Comparator;

final class SingleVariantDimensionComparator<T extends Dimension> implements Comparator<VariantInternal> {
	private final DimensionType<T> type;
	private final Comparator<T> delegate;

	protected SingleVariantDimensionComparator(DimensionType<T> type, Comparator<T> delegate) {
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
