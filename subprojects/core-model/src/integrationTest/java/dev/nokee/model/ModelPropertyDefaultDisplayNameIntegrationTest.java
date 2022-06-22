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
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelPropertyDefaultDisplayNameIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private ModelNode parent;
	private ModelNode subject;

	@BeforeEach
	void setUp() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		parent = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(ModelPropertyTag.class))
			.build());
	}

	@Test
	void hasDisplayNameComponent() {
		parent.addComponent(new DisplayNameComponent("project ':'"));
		subject.addComponent(new ElementNameComponent("myProperty"));
		subject.addComponent(new ParentComponent(parent));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("project ':' property 'myProperty'")));
	}

	@Test
	void createsDisplayNameEvenWhenParentHasNotDisplayName() {
		parent.addComponent(new ModelPathComponent(ModelPath.path("a.b.c")));
		subject.addComponent(new ParentComponent(parent));
		subject.addComponent(new ElementNameComponent("myProperty"));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("entity 'a.b.c' property 'myProperty'")));
	}

	@Test
	void createsDisplayNameEvenWhenParentHasNotDisplayNameAndIsRootEntity() {
		parent.addComponent(new ModelPathComponent(ModelPath.root()));
		subject.addComponent(new ParentComponent(parent));
		subject.addComponent(new ElementNameComponent("myProperty"));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("entity '<root>' property 'myProperty'")));
	}

	@Test
	void doesNotOverrideDisplayNameIfPresent() {
		subject.addComponent(new ParentComponent(parent));
		subject.addComponent(new ElementNameComponent("myProperty"));
		subject.addComponent(new DisplayNameComponent("my custom display name"));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("my custom display name")));
	}

	@Test
	void createsDisplayNameEvenWhenNoParent() {
		subject.addComponent(new ElementNameComponent("myProperty"));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("property 'myProperty'")));
	}

	@Test
	void createsDisplayNameEvenWhenNoParentOrElementName() {
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("property")));
	}

	@Test
	void createsDisplayNameWhenNoElementName() {
		subject.addComponent(new ElementNameComponent("myProperty"));
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("property 'myProperty'")));
	}
}
