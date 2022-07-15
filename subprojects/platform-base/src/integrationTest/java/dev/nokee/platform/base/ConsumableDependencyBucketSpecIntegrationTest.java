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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.testers.NamedTester;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketTag;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.internal.DomainObjectEntities.entityOf;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class ConsumableDependencyBucketSpecIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.mergeFrom(entityOf(ConsumableDependencyBucketSpec.class))
			.withComponent(new FullyQualifiedNameComponent("radoLunu"))
			.build());
	}

	@Test
	void hasDependencyBucketTag() {
		assertTrue(entity.hasComponent(typeOf(IsDependencyBucket.class)));
	}

	@Test
	void hasConsumableDependencyBucketTag() {
		assertTrue(entity.hasComponent(typeOf(ConsumableDependencyBucketTag.class)));
	}

	@Test
	void hasConfigurableTag() {
		assertTrue(entity.hasComponent(typeOf(ConfigurableTag.class)));
	}

	@Nested
	class ConsumableDependencyBucketProjectionTest implements NamedTester, BucketDependenciesTester {
		ConsumableDependencyBucketSpec subject;

		@BeforeEach
		void realizeProjection() {
			subject = ModelNodeUtils.get(ModelStates.realize(entity), ConsumableDependencyBucketSpec.class);
		}

		@Override
		public ConsumableDependencyBucketSpec subject() {
			return subject;
		}

		@Test
		void isFullyQualifiedNamed() {
			assertThat(subject.getName(), equalTo("radoLunu"));
		}

		@Test
		void hasNoDependencies() {
			assertThat(subject.getDependencies(), providerOf(emptyIterable()));
		}
	}
}
