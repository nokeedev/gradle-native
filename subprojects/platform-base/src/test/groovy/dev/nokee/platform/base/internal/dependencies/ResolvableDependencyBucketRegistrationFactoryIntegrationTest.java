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

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.util.ProjectTestUtils.createChildProject;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescriptionScheme.forComponent;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@Subject(ResolvableDependencyBucketRegistrationFactory.class)
class ResolvableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ResolvableDependencyBucket> {
	@Override
	public ResolvableDependencyBucket createSubject(Project project) {
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), forComponent(ComponentName.of("common")));
		val bucket = create(registry(project.getObjects()), factory.create("test"));
		ModelStates.realize(ModelNodes.of(bucket));
		return bucket;
	}

	@Test
	void canGetAsLenientFileCollection() {
		val rootProject = rootProject();
		val producerProjectA = createChildProject(rootProject, "projectA");
		val producerProjectB = createChildProject(rootProject, "projectB");
		forProject(producerProjectB).createIfAbsent("test", asConsumable().andThen(forUsage("foo")).andThen(it -> it.getOutgoing().artifact(producerProjectB.file("foo"))));

		val subjectProject = createChildProject(rootProject);
		val subject = createSubject(subjectProject);
		subject.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, subjectProject.getObjects().named(Usage.class, "foo"));
		subject.addDependency(producerProjectA);
		subject.addDependency(producerProjectB);
		assertThat(subject.getAsLenientFileCollection(), contains(producerProjectB.file("foo")));
	}

	@Test
	void canGetAsFileCollection() {
		val rootProject = rootProject();
		val producerProject = createChildProject(rootProject, "project");
		forProject(producerProject).createIfAbsent("test", asConsumable().andThen(forUsage("bar")).andThen(it -> it.getOutgoing().artifact(producerProject.file("bar"))));

		val subjectProject = createChildProject(rootProject);
		val subject = createSubject(subjectProject);
		subject.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, subjectProject.getObjects().named(Usage.class, "bar"));
		subject.addDependency(producerProject);
		assertThat(subject.getAsFileCollection(), contains(producerProject.file("bar")));
	}

	@Test
	void realizeNodeWhenRealized() {
		val project = rootProject();
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project));
		val bucketProvider = registry(project.getObjects()).register(factory.create("test"));
		ModelNodeUtils.get(ModelNodes.of(bucketProvider), Configuration.class).resolve(); // Force resolve the configuration
		assertThat(ModelStates.getState(ModelNodes.of(bucketProvider)), equalTo(Realized));
	}

	@Test
	void canFurtherConfigureWhenModelNodeRealized() {
		val project = rootProject();
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project));
		val bucketProvider = registry(project.getObjects()).register(factory.create("test"));
		bucketProvider.configure(bucket -> {
			using(project.getObjects(), forUsage(Usage.JAVA_API)).execute(bucket.getAsConfiguration());
		});
		ModelNodeUtils.get(ModelNodes.of(bucketProvider), Configuration.class).resolve();
		assertThat(ModelStates.getState(ModelNodes.of(bucketProvider)), equalTo(Realized));
	}
}
