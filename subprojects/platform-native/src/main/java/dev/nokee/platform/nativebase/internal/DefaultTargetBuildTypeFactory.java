package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.TargetBuildTypeFactory;
import dev.nokee.runtime.nativebase.TargetBuildType;

public class DefaultTargetBuildTypeFactory implements TargetBuildTypeFactory {
	public static final DefaultTargetBuildTypeFactory INSTANCE = new DefaultTargetBuildTypeFactory();
	public static final NamedTargetBuildType DEFAULT = new NamedTargetBuildType("default");

	@Override
	public TargetBuildType named(String name) {
		return new NamedTargetBuildType(name);
	}
}
