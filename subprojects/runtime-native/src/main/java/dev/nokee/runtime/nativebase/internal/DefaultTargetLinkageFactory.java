package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;

class DefaultTargetLinkageFactory implements TargetLinkageFactory {
	@Override
	public TargetLinkage getShared() {
		return TargetLinkages.SHARED;
	}

	@Override
	public TargetLinkage getStatic() {
		return TargetLinkages.STATIC;
	}
}
