package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;

import java.util.Comparator;

final class PreferSharedBinaryLinkageComparator implements Comparator<BinaryLinkage> {
	@Override
	public int compare(BinaryLinkage lhs, BinaryLinkage rhs) {
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
