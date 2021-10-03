/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.io.File;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static org.hamcrest.MatcherAssert.assertThat;

class ConsumableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ConsumableDependencyBucket> {
	@Override
	public ConsumableDependencyBucket createSubject(Project project) {
		val factory = new ConsumableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), ConfigurationDescriptionScheme.forComponent(ComponentName.of("common")));
		val bucket = create(registry(project.getObjects()), factory.create("test"));
		ModelStates.realize(ModelNodes.of(bucket));
		return bucket;
	}

	@Test
	void canAddProvidedArtifact() {
		val project = rootProject();
		createSubject(project).artifact(project.getLayout().getBuildDirectory().file("foo"));
		assertThat(project, hasConfiguration(hasPublishArtifact(ofFile(new File(project.getBuildDir(), "foo")))));
	}
}
