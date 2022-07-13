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
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.model.internal.core.ModelNodeUtils.canBeViewedAs;
import static dev.nokee.model.internal.core.ModelNodeUtils.get;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketConfigurationProjectionIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class)).build());
	}

	@Test
	void createsConfigurationObjectForDependencyBucketEntityWithFullyQualifiedName(Project project) {
		subject.addComponent(new FullyQualifiedNameComponent("kelfFlow"));
		assertThat(project.getConfigurations(), hasItem(named("kelfFlow")));
	}

	@Test
	void reusesConfigurationObjectIfPresent(Project project) {
		project.getConfigurations().create("jekeLows");
		assertDoesNotThrow(() -> subject.addComponent(new FullyQualifiedNameComponent("jekeLows")));
	}

	@Test
	void addsConfigurationProjectionToDependencyBucketEntity() {
		subject.addComponent(new FullyQualifiedNameComponent("lopeFlom"));
		assertTrue(canBeViewedAs(subject, of(Configuration.class)));
		assertThat(get(subject, of(Configuration.class)), named("lopeFlom"));
		assertThat(subject.get(ConfigurationComponent.class).get(), named("lopeFlom"));
	}
}
