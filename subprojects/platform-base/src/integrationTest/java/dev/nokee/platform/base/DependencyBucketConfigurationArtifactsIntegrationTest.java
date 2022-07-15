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

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BucketArtifacts;
import dev.nokee.platform.base.internal.dependencies.BucketDependencies;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.ConfigurationMatchers.ofFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketConfigurationArtifactsIntegrationTest {
	NamedDomainObjectProvider<Configuration> configuration;
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ConfigurationComponent(configuration = project.getConfigurations().register("lope")))
			.build());
		subject.addComponent(new BucketArtifacts(ImmutableSet.of(new LazyPublishArtifact(providerFactory().provider(() -> project.file("foo.txt"))), new LazyPublishArtifact(providerFactory().provider(() -> project.file("bar.txt"))))));
	}

	@Test
	void attachesBucketArtifactsToConfigurationProjection() {
		assertThat(configuration.get().getArtifacts(), hasItems(
			ofFile(withAbsolutePath(endsWith("/foo.txt"))), ofFile(withAbsolutePath(endsWith("/bar.txt")))
		));
	}
}
