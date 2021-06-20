package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributeMatchingStrategy;

public /*final*/ abstract class RuntimeNativePlugin implements Plugin<Project> {
	public static final TargetMachineFactory TARGET_MACHINE_FACTORY = new DefaultTargetMachineFactory();
	public static final TargetLinkageFactory TARGET_LINKAGE_FACTORY = new DefaultTargetLinkageFactory();

	@Override
	public void apply(Project project) {
		project.getDependencies().getAttributesSchema()
			.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE).getDisambiguationRules().add(BinaryLinkageSelectionRule.class);

		project.getDependencies().getAttributesSchema()
			.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, this::configureBuildTypeRules);
	}

	private void configureBuildTypeRules(AttributeMatchingStrategy<BuildType> strategy) {
		strategy.getDisambiguationRules().add(BuildTypeSelectionRule.class);
		strategy.getCompatibilityRules().add(BuildTypeCompatibilityRule.class);
	}
}
