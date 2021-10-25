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
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.ProjectMatchers.extensions;
import static dev.nokee.internal.testing.ProjectMatchers.publicType;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.gradle.api.reflect.TypeOf.typeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class ConsumableDependencyBucketRegistrationFactoryIntegrationTest extends AbstractPluginTest {
	private final ModelType<NamedDomainObjectProvider<Configuration>> PROVIDER_TYPE = ModelType.of(new TypeOf<NamedDomainObjectProvider<Configuration>>() {});
	private final ConsumableDependencyBucketRegistrationFactory subject = new ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project)), project.getObjects());
	private ModelElement element;

	@BeforeEach
	void setup() {
		element = project().getExtensions().getByType(ModelRegistry.class).register(subject.create(ModelPath.path("vonu"), DependencyBucketIdentifier.of(DependencyBucketName.of("vonu"), ConsumableDependencyBucket.class, ProjectIdentifier.of(project()))));
	}

	@Test
	void hasName() {
		assertEquals("vonu", element.getName());
	}

	@Nested
	class InstanceOfTest {
		@Test
		void isConsumableDependencyBucket() {
			assertTrue(element.instanceOf(ConsumableDependencyBucket.class));
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
		public ConsumableDependencyBucket subject() {
			return element.as(ConsumableDependencyBucket.class).get();
		}

		@Test
		void hasBucketName() {
			assertThat(subject().getName(), equalTo("vonu"));
		}

		@Test
		void canAddDependency() {
			subject().addDependency("com.example:foo:4.2");
			assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
		}

		@Test
		void canAddDependencyWithConfigurationAction() {
			subject().addDependency("com.example:foo:4.2", doSomething());
			assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
		}

		@Test
		void canGetAsConfiguration() {
			assertThat(subject().getAsConfiguration(), allOf(named("vonu"), isA(Configuration.class)));
		}

		@Test
		void canConfigureDependencyBeforeAddingIt() {
			val action = ActionTestUtils.mockAction(ModuleDependency.class);
			subject().addDependency("com.example:foo:4.2", action);
			assertThat(action, calledOnceWith(singleArgumentOf(forCoordinate("com.example:foo:4.2"))));
		}
	}

	@Nested
	class ConfigurationTest {
		Configuration subject() {
			return project().getConfigurations().getByName("vonu");
		}

		@Test
		void isConsumableConfiguration() {
			assertThat(subject(), consumable());
		}

		@Test
		void hasDescription() {
			assertThat(subject(), description("Vonu for project ':'."));
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
		void hasDependencyBucketExtension() {
			assertThat(subject(), extensions(hasItem(publicType(typeOf(ConsumableDependencyBucket.class)))));
		}

		@Test
		void doesNotRealizeNodeWhenConfigurationIsRealized() {
			assertFalse(ModelStates.getState(ModelNodes.of(element)).isAtLeast(ModelState.Realized));
		}

		@Test
		void realizeNodeWhenConfigurationIsResolved() {
			((ConfigurationInternal) subject()).preventFromFurtherMutation();
			assertTrue(ModelStates.getState(ModelNodes.of(element)).isAtLeast(ModelState.Realized));
		}
	}

	@Test
	void canAddProvidedArtifact() {
		element.as(ConsumableDependencyBucket.class).get().artifact(project.getLayout().getBuildDirectory().file("foo"));
		assertThat(project, hasConfiguration(hasPublishArtifact(ofFile(new File(project.getBuildDir(), "foo")))));
	}
}
