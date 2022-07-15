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
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketConfigurationFinalizerIntegrationTest {
	@Nested
	class GradleConfigurationExtendsFromNokeeDependencyBucketTest {
		NamedDomainObjectProvider<Configuration> bucketConfiguration;
		ModelNode dependencyBucket;
		Configuration subject;

		@BeforeEach
		void setUp(Project project) {
			dependencyBucket = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
				.withComponent(tag(IsDependencyBucket.class))
				.withComponent(new ConfigurationComponent(bucketConfiguration = project.getConfigurations().register("test")))
				.build());
			subject = project.getConfigurations().create("subject", configureExtendsFrom(bucketConfiguration));
		}

		@Test
		void finalizesParentNokeeBucketViaChildGradleConfiguration() {
			((ConfigurationInternal) subject).preventFromFurtherMutation();
			assertTrue(dependencyBucket.has(ModelState.IsAtLeastFinalized.class));
		}
	}

	@Nested
	class NokeeDependencyBucketExtendsFromNokeeDependencyBucketTest {
		NamedDomainObjectProvider<Configuration> bucketConfiguration;
		ModelNode dependencyBucket;
		NamedDomainObjectProvider<Configuration> subjectConfiguration;
		ModelNode subjectBucket;

		@BeforeEach
		void setUp(Project project) {
			dependencyBucket = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
				.withComponent(tag(IsDependencyBucket.class))
				.withComponent(new ConfigurationComponent(bucketConfiguration = project.getConfigurations().register("test")))
				.build());
			subjectBucket = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
				.withComponent(tag(IsDependencyBucket.class))
				.withComponent(new ConfigurationComponent(subjectConfiguration = project.getConfigurations().register("subject", configureExtendsFrom(bucketConfiguration))))
				.build());
		}

		@Test
		void finalizesParentNokeeBucketViaChildGradleConfiguration() {
			((ConfigurationInternal) subjectConfiguration.get()).preventFromFurtherMutation();
			assertTrue(dependencyBucket.has(ModelState.IsAtLeastFinalized.class));
		}

		@Test
		void finalizesParentNokeeBucketViaChildNokeeBucket() {
			ModelStates.finalize(subjectBucket);
			assertTrue(dependencyBucket.has(ModelState.IsAtLeastFinalized.class));
		}
	}

	@Nested
	class NokeeDependencyBucketExtendsFromGradleConfigurationExtendsFromNokeeDependencyBucketTest {
		NamedDomainObjectProvider<Configuration> topMostBucketConfiguration;
		ModelNode topMostDependencyBucket;
		Configuration middleConfiguration;
		NamedDomainObjectProvider<Configuration> subjectConfiguration;
		ModelNode subjectDependencyBucket;

		@BeforeEach
		void setUp(Project project) {
			topMostDependencyBucket = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
				.withComponent(tag(IsDependencyBucket.class))
				.withComponent(new ConfigurationComponent(topMostBucketConfiguration = project.getConfigurations().register("topMost")))
				.build());
			middleConfiguration = project.getConfigurations().create("middle", configureExtendsFrom(topMostBucketConfiguration));
			subjectDependencyBucket = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
				.withComponent(tag(IsDependencyBucket.class))
				.withComponent(new ConfigurationComponent(subjectConfiguration = project.getConfigurations().register("subject", configureExtendsFrom(middleConfiguration))))
				.build());
		}

		@Test
		void finalizesTopMostNokeeBucketViaChildGradleConfiguration() {
			((ConfigurationInternal) subjectConfiguration.get()).preventFromFurtherMutation();
			assertTrue(topMostDependencyBucket.has(ModelState.IsAtLeastFinalized.class));
		}

		@Test
		void finalizesParentNokeeBucketViaChildNokeeBucket() {
			ModelStates.finalize(subjectDependencyBucket);
			assertTrue(topMostDependencyBucket.has(ModelState.IsAtLeastFinalized.class));
		}
	}
}
