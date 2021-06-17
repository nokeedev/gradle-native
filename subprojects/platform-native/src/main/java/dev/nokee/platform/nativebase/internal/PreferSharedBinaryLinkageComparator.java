package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultBinaryLinkage;

import java.util.Comparator;

final class PreferSharedBinaryLinkageComparator implements Comparator<TargetLinkage> {
	@Override
	public int compare(TargetLinkage lhs, TargetLinkage rhs) {
		return compare((DefaultBinaryLinkage) lhs,  (DefaultBinaryLinkage) rhs);
	}

	private int compare(DefaultBinaryLinkage lhs, DefaultBinaryLinkage rhs) {
		if (lhs.isShared() && rhs.isShared()) {
			return 0;
		} else if (lhs.isShared()) {
			return -1;
		} else if (rhs.isShared()) {
			return 1;
		}
		return 0;
	}
}
