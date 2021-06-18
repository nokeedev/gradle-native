package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributeMatchingStrategy;

public /*final*/ abstract class RuntimeNativePlugin implements Plugin<Project> {
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
