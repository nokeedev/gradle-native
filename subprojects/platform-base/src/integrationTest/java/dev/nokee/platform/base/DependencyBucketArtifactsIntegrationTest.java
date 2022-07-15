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
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BucketArtifacts;
import dev.nokee.platform.base.internal.dependencies.BucketArtifactsProperty;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.PublishedArtifactElement;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketTag;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.ConfigurationMatchers.ofFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.model.internal.core.ModelProperties.add;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.state.ModelStates.discover;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketArtifactsIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void setUp(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.build());
	}

	@Test
	void doesNotHaveBucketArtifactsPropertyOnDeclarableBuckets() {
		subject.addComponent(tag(DeclarableDependencyBucketTag.class));
		assertFalse(subject.has(BucketArtifactsProperty.class));
		assertFalse(discover(subject).has(BucketArtifactsProperty.class));
	}

	@Test
	void doesNotHaveBucketArtifactsPropertyOnResolvableBuckets() {
		subject.addComponent(tag(ResolvableDependencyBucketTag.class));
		assertFalse(subject.has(BucketArtifactsProperty.class));
		assertFalse(discover(subject).has(BucketArtifactsProperty.class));
	}

	@Nested
	class ConsumableDependencyBucketTest {
		@BeforeEach
		void setUp() {
			subject.addComponent(tag(ConsumableDependencyBucketTag.class));
		}

		@Test
		void hasBucketArtifactsPropertyWhenDiscovered() {
			assertFalse(subject.has(BucketArtifactsProperty.class));
			assertTrue(discover(subject).has(BucketArtifactsProperty.class));
		}

		@Test
		void hasBucketArtifactsWhenBucketFinalized() {
			assertFalse(subject.has(BucketArtifacts.class));
			assertFalse(discover(subject).has(BucketArtifacts.class));
			assertTrue(ModelStates.finalize(subject).has(BucketArtifacts.class));
		}

		@Test
		void syncsBucketArtifactsFromPropertyToComponentWhenFinalized(Project project) {
			discover(subject);
			add(subject.get(BucketArtifactsProperty.class).get(), new PublishedArtifactElement(providerFactory().provider(() -> project.file("lkle.txt"))));
			add(subject.get(BucketArtifactsProperty.class).get(), new PublishedArtifactElement(providerFactory().provider(() -> project.file("kels.txt"))));
			assertThat(ModelStates.finalize(subject).get(BucketArtifacts.class).get(), containsInAnyOrder(
				ofFile(withAbsolutePath(endsWith("/lkle.txt"))), ofFile(withAbsolutePath(endsWith("/kels.txt")))
			));
		}
	}
}
