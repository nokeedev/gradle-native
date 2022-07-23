/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullyQualifiedNameIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private ModelNode grandParent;
	private ModelNode parent;
	private ModelNode subject;

	@BeforeEach
	void applyPlugins() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		grandParent = registry.instantiate(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("xehu"))
			.build()
		);
		parent = registry.instantiate(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("lifu"))
			.withComponent(new ParentComponent(grandParent))
			.build()
		);
		subject = registry.instantiate(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("jaqi"))
			.withComponent(new ParentComponent(parent))
			.build());
	}

	@Test
	void hasFullyQualifiedNameComponentOnlyIfElementNameAndNamingSchemePresent() {
		assertTrue(subject.has(FullyQualifiedNameComponent.class));
		assertFalse(parent.has(FullyQualifiedNameComponent.class));
		assertFalse(grandParent.has(FullyQualifiedNameComponent.class));
	}

	@Test
	void hasNamesRelativeToItsParents() {
		assertThat(subject.get(FullyQualifiedNameComponent.class).get(), equalTo(FullyQualifiedName.of("xehuLifuJaqi")));
	}
}
