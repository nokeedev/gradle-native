package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;

public class DefaultTargetLinkageFactory implements TargetLinkageFactory {
	public static final DefaultTargetLinkageFactory INSTANCE = new DefaultTargetLinkageFactory();

	@Override
	public TargetLinkage getShared() {
		return TargetLinkages.SHARED;
	}

	@Override
	public TargetLinkage getStatic() {
		return TargetLinkages.STATIC;
	}
}
