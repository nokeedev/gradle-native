package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;

public final class TargetBuildTypes {
	private TargetBuildTypes() {}

	public static TargetBuildType DEFAULT = new DefaultTargetBuildType(BuildType.named("default"));

	public static TargetBuildType named(String name) {
		return new DefaultTargetBuildType(BuildType.named(name));
	}
}
