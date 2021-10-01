/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.runtime.base;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
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
