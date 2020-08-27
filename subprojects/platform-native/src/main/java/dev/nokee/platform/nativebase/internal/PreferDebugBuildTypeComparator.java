package dev.nokee.platform.nativebase.internal;

import java.util.Comparator;

final class PreferDebugBuildTypeComparator implements Comparator<BaseTargetBuildType> {
	@Override
	public int compare(BaseTargetBuildType lhs, BaseTargetBuildType rhs) {
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
