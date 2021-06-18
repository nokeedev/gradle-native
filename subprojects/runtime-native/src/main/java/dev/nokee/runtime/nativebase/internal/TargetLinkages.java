package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;

import static dev.nokee.runtime.nativebase.BinaryLinkage.named;

public final class TargetLinkages {
	private TargetLinkages() {}

	public static final TargetLinkage SHARED = new DefaultTargetLinkage(named(BinaryLinkage.SHARED));

	public static final TargetLinkage STATIC = new DefaultTargetLinkage(named(BinaryLinkage.STATIC));

	public static final TargetLinkage BUNDLE = new DefaultTargetLinkage(named(BinaryLinkage.BUNDLE));

	public static final TargetLinkage EXECUTABLE = new DefaultTargetLinkage(named(BinaryLinkage.EXECUTABLE));
}
