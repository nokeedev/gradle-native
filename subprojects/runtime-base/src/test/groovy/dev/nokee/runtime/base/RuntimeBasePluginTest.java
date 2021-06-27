package dev.nokee.runtime.base;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.nativeplatform.OperatingSystemFamily;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

class RuntimeBasePluginTest {
	private static Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply(RuntimeBasePlugin.class);
		return project;
	}

	@Test
	void registerUsageAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(Usage.USAGE_ATTRIBUTE)));
	}

	@Test
	void registerLibraryElementsAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE)));
	}

	@Test
	void registerCategoryAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(Category.CATEGORY_ATTRIBUTE)));
	}
}
