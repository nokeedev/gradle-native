package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.TargetLinkageFactory;
import dev.nokee.runtime.nativebase.TargetLinkage;

public class DefaultTargetLinkageFactory implements TargetLinkageFactory {
	public static final DefaultTargetLinkageFactory INSTANCE = new DefaultTargetLinkageFactory();

	@Override
	public TargetLinkage getShared() {
		return DefaultBinaryLinkage.SHARED;
	}

	@Override
	public TargetLinkage getStatic() {
		return DefaultBinaryLinkage.STATIC;
	}
}
