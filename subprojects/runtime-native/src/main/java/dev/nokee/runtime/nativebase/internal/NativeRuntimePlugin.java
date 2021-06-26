package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributeMatchingStrategy;
import org.gradle.api.attributes.AttributesSchema;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE;

public /*final*/ abstract class NativeRuntimePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getDependencies().getAttributesSchema()
			.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, this::configureBuildTypeRules);
		project.getDependencies().attributesSchema(this::configureAttributesSchema);
	}

	private void configureBuildTypeRules(AttributeMatchingStrategy<BuildType> strategy) {
		strategy.getDisambiguationRules().add(BuildTypeSelectionRule.class);
		strategy.getCompatibilityRules().add(BuildTypeCompatibilityRule.class);
	}

	private void configureAttributesSchema(AttributesSchema schema) {
		schema.attribute(BINARY_LINKAGE_ATTRIBUTE, new BinaryLinkageAttributeSchema());
	}
}
