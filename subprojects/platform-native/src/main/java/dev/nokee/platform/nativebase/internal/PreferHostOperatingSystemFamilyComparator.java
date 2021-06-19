package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.OperatingSystemFamily;

import java.util.Comparator;

final class PreferHostOperatingSystemFamilyComparator implements Comparator<OperatingSystemFamily> {
	private static final OperatingSystemFamily HOST = OperatingSystemFamily.forName(System.getProperty("os.name"));
	@Override
	public int compare(OperatingSystemFamily lhs, OperatingSystemFamily rhs) {
		if (lhs.equals(HOST) && rhs.equals(HOST)) {
			return 0;
		} else if (lhs.equals(HOST)) {
			return -1;
		} else if (rhs.equals(HOST)) {
			return 1;
		}
		return 0;
	}
}
