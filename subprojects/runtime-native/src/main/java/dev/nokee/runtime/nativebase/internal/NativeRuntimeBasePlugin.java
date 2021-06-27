package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import dev.nokee.runtime.nativebase.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributesSchema;

public /*final*/ abstract class NativeRuntimeBasePlugin implements Plugin<Project> {
	public static final TargetMachineFactory TARGET_MACHINE_FACTORY = new DefaultTargetMachineFactory();
	public static final TargetLinkageFactory TARGET_LINKAGE_FACTORY = new DefaultTargetLinkageFactory();
	public static final TargetBuildTypeFactory TARGET_BUILD_TYPE_FACTORY = new DefaultTargetBuildTypeFactory();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(RuntimeBasePlugin.class);
		project.getDependencies().attributesSchema(this::registerNativeAttributes);
	}
	private void registerNativeAttributes(AttributesSchema schema) {
		schema.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE);
		schema.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE);
		schema.attribute(BuildType.BUILD_TYPE_ATTRIBUTE);
		schema.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE);
	}
}
