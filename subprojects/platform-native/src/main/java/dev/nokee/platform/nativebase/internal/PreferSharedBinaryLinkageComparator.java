package dev.nokee.platform.nativebase.internal;

import java.util.Comparator;

final class PreferSharedBinaryLinkageComparator implements Comparator<DefaultBinaryLinkage> {
	@Override
	public int compare(DefaultBinaryLinkage lhs, DefaultBinaryLinkage rhs) {
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
