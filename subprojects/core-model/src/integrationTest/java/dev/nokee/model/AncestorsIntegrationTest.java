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
import dev.nokee.model.internal.ancestors.AncestorsComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.ancestors.AncestorRef.of;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class AncestorsIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private ModelRegistry registry;
	private ModelNode greatGrandParent;
	private ModelNode grandParent;
	private ModelNode parent;
	private ModelNode child;

	@BeforeEach
	void setUp() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		registry = project.getExtensions().getByType(ModelRegistry.class);
		greatGrandParent = registry.instantiate(builder().build());
		grandParent = registry.instantiate(builder().withComponent(new ParentComponent(greatGrandParent)).build());
		parent = registry.instantiate(builder().withComponent(new ParentComponent(grandParent)).build());
		child = registry.instantiate(builder().withComponent(new ParentComponent(parent)).build());
	}

	@Test
	void hasAncestorsComponentOnlyIfItHasParent() {
		assertThat(greatGrandParent.hasComponent(AncestorsComponent.class), is(false));
		assertThat(grandParent.hasComponent(AncestorsComponent.class), is(true));
		assertThat(parent.hasComponent(AncestorsComponent.class), is(true));
		assertThat(child.hasComponent(AncestorsComponent.class), is(true));
	}

	@Test
	void doesNotContainsSelfInAncestors() {
		assertThat(grandParent.getComponent(AncestorsComponent.class).get(), not(hasItem(of(grandParent))));
		assertThat(parent.getComponent(AncestorsComponent.class).get(), not(hasItem(of(parent))));
		assertThat(child.getComponent(AncestorsComponent.class).get(), not(hasItem(of(child))));
	}

	@Test
	void containsAllParentsInAncestors() {
		assertThat(grandParent.getComponent(AncestorsComponent.class).get(), containsInAnyOrder(of(greatGrandParent)));
		assertThat(parent.getComponent(AncestorsComponent.class).get(), containsInAnyOrder(of(greatGrandParent), of(grandParent)));
		assertThat(child.getComponent(AncestorsComponent.class).get(), containsInAnyOrder(of(greatGrandParent), of(grandParent), of(parent)));
	}
}
