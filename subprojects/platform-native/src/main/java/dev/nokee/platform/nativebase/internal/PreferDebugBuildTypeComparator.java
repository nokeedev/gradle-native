package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.BuildType;

import java.util.Comparator;

final class PreferDebugBuildTypeComparator implements Comparator<BuildType> {
	@Override
	public int compare(BuildType lhs, BuildType rhs) {
		if (lhs.getName().equalsIgnoreCase(rhs.getName())) {
			return 0;
		} else if (rhs.getName().equalsIgnoreCase("debug")) {
			return 1;
		} else if (lhs.getName().equalsIgnoreCase("debug")) {
			return -1;
		}
		return 0;
	}
}
