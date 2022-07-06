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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.dependencies;
import static dev.nokee.internal.testing.ConfigurationMatchers.description;
import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.ConfigurationMatchers.hasConfiguration;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createChildProject;
import static dev.nokee.model.internal.core.ModelNodes.of;
import static dev.nokee.model.internal.state.ModelState.Finalized;
import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class ResolvableDependencyBucketRegistrationFactoryIntegrationTest extends AbstractPluginTest {
	private final ModelType<NamedDomainObjectProvider<Configuration>> PROVIDER_TYPE = ModelType.of(new TypeOf<NamedDomainObjectProvider<Configuration>>() {});
	private ResolvableDependencyBucketRegistrationFactory subject;
	private ModelElement element;

	@BeforeEach
	void setup() {
		subject = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
		element = project().getExtensions().getByType(ModelRegistry.class).register(subject.create(DependencyBucketIdentifier.of("goju", ProjectIdentifier.of(project()))));
	}

	@Test
	void hasName() {
		assertEquals("goju", element.getName());
	}

	@Nested
	class InstanceOfTest {
		@Test
		void isResolvableDependencyBucket() {
			assertTrue(element.instanceOf(ResolvableDependencyBucket.class));
		}

		@Test
		void isConfiguration() {
			assertTrue(element.instanceOf(Configuration.class));
		}

		@Test
		void isNamedDomainObjectProvider() {
			assertTrue(element.instanceOf(NamedDomainObjectProvider.class));
		}

		@Test
		void isNamedDomainObjectProviderOfConfiguration() {
			assertTrue(element.instanceOf(PROVIDER_TYPE));
		}
	}

	@Nested
	class DependencyBucketInstanceTest {
		public ResolvableDependencyBucket subject() {
			return element.as(ResolvableDependencyBucket.class).get();
		}

		@Test
		void hasBucketName() {
			assertThat(subject().getName(), equalTo("goju"));
		}

		@Test
		void canAddDependency() {
			subject().addDependency("com.example:foo:4.2");
			ModelStates.finalize(of(subject()));
			assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
		}

		@Test
		void canAddDependencyWithConfigurationAction() {
			subject().addDependency("com.example:foo:4.2", doSomething());
			ModelStates.finalize(of(subject()));
			assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
		}

		@Test
		void canGetAsConfiguration() {
			assertThat(subject().getAsConfiguration(), allOf(named("goju"), isA(Configuration.class)));
		}

		@Test
		void canConfigureDependencyBeforeAddingIt() {
			val action = ActionTestUtils.mockAction(ModuleDependency.class);
			subject().addDependency("com.example:foo:4.2", action);
			ModelStates.finalize(of(subject()));
			assertThat(action, calledOnceWith(singleArgumentOf(forCoordinate("com.example:foo:4.2"))));
		}
	}

	@Nested
	class ConfigurationTest {
		Configuration subject() {
			return project().getConfigurations().getByName("goju");
		}

		@Test
		void isConsumableConfiguration() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), description("Goju for project ':'."));
		}

		@Test
		void hasConfiguration() {
			assertEquals(subject(), element.as(Configuration.class).get());
		}

		@Test
		void hasNamedDomainObjectProviderOfConfiguration() {
			assertThat(element.as(PROVIDER_TYPE).get(), providerOf(subject()));
		}

		@Test
		void doesNotRealizeNodeWhenConfigurationIsRealized() {
			assertFalse(ModelStates.getState(of(element)).isAtLeast(Realized));
		}

		@Test
		void realizeNodeWhenConfigurationIsResolved() {
			subject().resolve();
			assertTrue(ModelStates.getState(of(element)).isAtLeast(Finalized));
		}
	}

	@Test
	void canGetAsLenientFileCollection() {
		val producerProjectA = createChildProject(project, "projectA");
		val producerProjectB = createChildProject(project, "projectB");
		producerProjectB.getConfigurations().create("test", ConfigurationUtils.configureAsConsumable()
			.andThen(ConfigurationUtils.configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, "foo"))))
			.andThen(it -> it.getOutgoing().artifact(producerProjectB.file("foo"))));

		val bucket = element.as(ResolvableDependencyBucket.class).get();
		bucket.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "foo"));
		bucket.addDependency(producerProjectA);
		bucket.addDependency(producerProjectB);
		ModelStates.finalize(of(bucket));
		assertThat(bucket.getAsLenientFileCollection(), contains(producerProjectB.file("foo")));
	}

	@Test
	void canGetAsFileCollection() {
		val producerProject = createChildProject(project, "project");
		producerProject.getConfigurations().create("test", ConfigurationUtils.configureAsConsumable()
			.andThen(ConfigurationUtils.configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, "bar"))))
			.andThen(it -> it.getOutgoing().artifact(producerProject.file("bar"))));

		val bucket = element.as(ResolvableDependencyBucket.class).get();
		bucket.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "bar"));
		bucket.addDependency(producerProject);
		ModelStates.finalize(of(bucket));
		assertThat(bucket.getAsFileCollection(), contains(producerProject.file("bar")));
	}

	@Test
	void canFurtherConfigureWhenModelNodeRealized() {
		val bucketProvider = element.as(ResolvableDependencyBucket.class);
		bucketProvider.configure(bucket -> {
			bucket.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
		});
		ModelNodeUtils.get(of(bucketProvider), Configuration.class).resolve();
		assertThat(ModelStates.getState(of(bucketProvider)), equalTo(Finalized));
	}
}
