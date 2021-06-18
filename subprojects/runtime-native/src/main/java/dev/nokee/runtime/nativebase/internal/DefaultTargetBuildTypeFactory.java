package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetBuildTypeFactory;

public class DefaultTargetBuildTypeFactory implements TargetBuildTypeFactory {
	public static final DefaultTargetBuildTypeFactory INSTANCE = new DefaultTargetBuildTypeFactory();

	@Override
	public TargetBuildType named(String name) {
		return new DefaultTargetBuildType(BuildType.named(name));
	}
}
