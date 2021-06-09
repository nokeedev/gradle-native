package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;

import java.util.Comparator;

final class PreferHostOperatingSystemFamilyComparator implements Comparator<OperatingSystemFamily> {
	@Override
	public int compare(OperatingSystemFamily lhs, OperatingSystemFamily rhs) {
		if (lhs.equals(DefaultOperatingSystemFamily.HOST) && rhs.equals(DefaultOperatingSystemFamily.HOST)) {
			return 0;
		} else if (lhs.equals(DefaultOperatingSystemFamily.HOST)) {
			return -1;
		} else if (rhs.equals(DefaultOperatingSystemFamily.HOST)) {
			return 1;
		}
		return 0;
	}
}
