package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;

class TargetLinkageFactoryTest implements TargetLinkageFactoryTester {
	@Override
	public TargetLinkageFactory subject() {
		return NativeRuntimeBasePlugin.TARGET_LINKAGE_FACTORY;
	}
}
