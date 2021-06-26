package dev.nokee.runtime.nativebase.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributesSchema;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.BuildType.BUILD_TYPE_ATTRIBUTE;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;

public /*final*/ abstract class NativeRuntimePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getDependencies().attributesSchema(this::configureAttributesSchema);
	}

	private void configureAttributesSchema(AttributesSchema schema) {
		schema.attribute(BINARY_LINKAGE_ATTRIBUTE, new BinaryLinkageAttributeSchema());
		schema.attribute(BUILD_TYPE_ATTRIBUTE, new BuildTypeAttributeSchema());
		schema.attribute(USAGE_ATTRIBUTE, new UsageAttributeSchema());
	}
}
