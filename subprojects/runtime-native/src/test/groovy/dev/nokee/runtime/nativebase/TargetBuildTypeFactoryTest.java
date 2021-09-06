package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;

class TargetBuildTypeFactoryTest implements TargetBuildTypeFactoryTester {
	@Override
	public TargetBuildTypeFactory subject() {
		return NativeRuntimeBasePlugin.TARGET_BUILD_TYPE_FACTORY;
	}
}
