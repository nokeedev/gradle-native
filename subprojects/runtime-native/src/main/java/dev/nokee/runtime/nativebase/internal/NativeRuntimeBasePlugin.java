package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetBuildTypeFactory;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public /*final*/ abstract class NativeRuntimeBasePlugin implements Plugin<Project> {
	public static final TargetMachineFactory TARGET_MACHINE_FACTORY = new DefaultTargetMachineFactory();
	public static final TargetLinkageFactory TARGET_LINKAGE_FACTORY = new DefaultTargetLinkageFactory();
	public static final TargetBuildTypeFactory TARGET_BUILD_TYPE_FACTORY = new DefaultTargetBuildTypeFactory();

	@Override
	public void apply(Project project) {
	}
}
