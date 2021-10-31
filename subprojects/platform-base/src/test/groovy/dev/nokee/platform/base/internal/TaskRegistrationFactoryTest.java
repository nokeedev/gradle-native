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
package dev.nokee.platform.base.internal;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class TaskRegistrationFactoryTest extends AbstractPluginTest {
	private final ModelType<TaskProvider<Task>> PROVIDER_TYPE = ModelType.of(new TypeOf<TaskProvider<Task>>() {});
	private TaskRegistrationFactory subject;
	private ModelElement element;

	@BeforeEach
	void setup() {
		subject = project.getExtensions().getByType(TaskRegistrationFactory.class);
		element = project().getExtensions().getByType(ModelRegistry.class).register(subject.create(TaskIdentifier.of(TaskName.of("ziso"), Task.class, ProjectIdentifier.of(project())), Task.class).build());
	}

	@Test
	void hasName() {
		assertEquals("ziso", element.getName());
	}

	@Nested
	class InstanceOfTest {
		@Test
		void isTask() {
			assertTrue(element.instanceOf(Task.class));
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

	@Test
	void realizesNodeWhenTaskIsRealized() {
		assertFalse(ModelStates.getState(ModelNodes.of(element)).isAtLeast(ModelState.Realized));
		element.as(Task.class).get();
		assertTrue(ModelStates.getState(ModelNodes.of(element)).isAtLeast(ModelState.Realized));
	}

	@Nested
	class TaskTest {
		Task subject() {
			return project().getTasks().getByName("ziso");
		}

		@Test
		void hasNoDescription() {
			assertThat(subject(), TaskMatchers.description(Matchers.nullValue(String.class)));
		}

		@Test
		void hasTask() {
			assertEquals(subject(), element.as(Task.class).get());
		}

		@Test
		void hasNamedDomainObjectProviderOfTask() {
			assertThat(element.as(PROVIDER_TYPE).get(), providerOf(subject()));
		}
	}
}
