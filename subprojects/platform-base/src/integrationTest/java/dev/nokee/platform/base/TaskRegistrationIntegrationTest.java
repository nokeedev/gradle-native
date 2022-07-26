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
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskProjectionComponent;
import dev.nokee.platform.base.internal.tasks.TaskTypeComponent;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.canBeViewedAs;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class TaskRegistrationIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsTask.class))
			.withComponent(new FullyQualifiedNameComponent("klseWlde"))
			.withComponent(new TaskTypeComponent(MyTask.class))
			.build());
	}

	@Test
	void hasTaskProjectionComponent() {
		assertTrue(subject.has(TaskProjectionComponent.class));
		assertThat(subject.get(TaskProjectionComponent.class).get(),
			providerOf(allOf(named("klseWlde"), isA(MyTask.class))));
	}

	@Test
	void hasModelElementProviderSourceComponent() {
		assertTrue(subject.has(ModelElementProviderSourceComponent.class));
		assertThat(subject.get(ModelElementProviderSourceComponent.class).get(),
			providerOf(allOf(named("klseWlde"), isA(MyTask.class))));
	}

	@Test
	void hasTaskProviderProjection() {
		assertTrue(canBeViewedAs(subject, of(TaskProvider.class)));
	}

	@Test
	void hasTaskProjection() {
		assertTrue(canBeViewedAs(subject, of(MyTask.class)));
	}

	public static class MyTask extends DefaultTask {}
}
