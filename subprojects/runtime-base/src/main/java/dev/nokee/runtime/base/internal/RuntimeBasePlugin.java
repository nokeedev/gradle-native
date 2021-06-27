package dev.nokee.runtime.base.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;

public /*final*/ abstract class RuntimeBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getDependencies().attributesSchema(this::registerBaseAttributes);
	}

	private void registerBaseAttributes(AttributesSchema schema) {
		schema.attribute(Usage.USAGE_ATTRIBUTE);
		schema.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE);
		schema.attribute(Category.CATEGORY_ATTRIBUTE);
	}
}
