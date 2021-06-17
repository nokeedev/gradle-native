package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.internal.NamedTargetBuildType;

import java.util.Comparator;

final class PreferDebugBuildTypeComparator implements Comparator<TargetBuildType> {
	@Override
	public int compare(TargetBuildType lhs, TargetBuildType rhs) {
		if (lhs instanceof NamedTargetBuildType) {
			if (rhs instanceof NamedTargetBuildType) {
				if (((NamedTargetBuildType) lhs).getName().equalsIgnoreCase(((NamedTargetBuildType) rhs).getName())) {
					return 0;
				} else if (((NamedTargetBuildType) rhs).getName().equalsIgnoreCase("debug")) {
					return 1;
				}
			}

			if (((NamedTargetBuildType) lhs).getName().equalsIgnoreCase("debug")) {
				return -1;
			}
		}
		return 0;
	}
}
