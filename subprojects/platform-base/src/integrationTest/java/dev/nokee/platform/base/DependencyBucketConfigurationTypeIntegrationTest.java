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
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketTag;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.ConfigurationMatchers.consumable;
import static dev.nokee.internal.testing.ConfigurationMatchers.declarable;
import static dev.nokee.internal.testing.ConfigurationMatchers.resolvable;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketConfigurationTypeIntegrationTest {
	NamedDomainObjectProvider<Configuration> configuration;
	ModelNode subject;

	@BeforeEach
	void setUp(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ConfigurationComponent(configuration = project.getConfigurations().register("test")))
			.build());
	}

	@Test
	void setsConfigurationAsConsumableForConsumableDependencyBucket() {
		subject.addComponent(tag(ConsumableDependencyBucketTag.class));
		assertThat(configuration.get(), consumable());
	}

	@Test
	void setsConfigurationAsResolvableForResolvableDependencyBucket() {
		subject.addComponent(tag(ResolvableDependencyBucketTag.class));
		assertThat(configuration.get(), resolvable());
	}

	@Test
	void setsConfigurationAsDeclarableForDeclarableDependencyBucket() {
		subject.addComponent(tag(DeclarableDependencyBucketTag.class));
		assertThat(configuration.get(), declarable());
	}
}
