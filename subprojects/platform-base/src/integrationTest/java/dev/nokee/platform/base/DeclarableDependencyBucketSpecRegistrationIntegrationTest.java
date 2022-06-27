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
package dev.nokee.platform.base;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GradleTestExtension.class)
@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class DeclarableDependencyBucketSpecRegistrationIntegrationTest {
	@GradleProject Project project;
	ModelNode subject;

	@BeforeEach
	void setup() {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(newEntity("lipa", ResolvableDependencyBucketSpec.class));
	}

	@Test
	void hasDeclarableDependencyBucketSpecTag() {
		assertTrue(subject.hasComponent(typeOf(DeclarableDependencyBucketSpec.Tag.class)));
	}

	@Test
	void hasDependencyBucketTag() {
		assertTrue(subject.hasComponent(typeOf(IsDependencyBucket.class)));
	}

	@Test
	void hasConfigurableTag() {
		assertTrue(subject.hasComponent(typeOf(ConfigurableTag.class)));
	}

	@Test
	void hasElementNameComponent() {
		assertThat(subject.find(ElementNameComponent.class).map(ElementNameComponent::get),
			optionalWithValue(equalTo(ElementName.of("lipa"))));
	}

	@Test
	void hasNoDefaultDisplayName() {
		assertThat(subject.find(DisplayNameComponent.class), emptyOptional());
	}

	@Test
	void hasNoDefaultIdentifier() {
		assertThat(subject.find(IdentifierComponent.class), emptyOptional());
	}
}
