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
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPropertyTag;
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

class ModelPropertyIdentifierAsDisplayNameIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private ModelNode subject;

	@BeforeEach
	void setUp() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		subject = ModelNodes.of(project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(ProjectIdentifier.of(project), "myProperty")))
			.withComponent(tag(ModelPropertyTag.class))
			.build()));
	}

	@Test
	void hasDisplayNameComponent() {
		assertThat(subject.getComponent(componentOf(DisplayNameComponent.class)).get(), equalTo(new DisplayName("project ':' property 'myProperty'")));
	}
}
