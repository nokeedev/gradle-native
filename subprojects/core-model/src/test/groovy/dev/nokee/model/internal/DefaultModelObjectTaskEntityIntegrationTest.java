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
package dev.nokee.model.internal;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class DefaultModelObjectTaskEntityIntegrationTest extends AbstractPluginTest {
	private DomainObjectProvider<Task> subject;

	@BeforeEach
	void createSubject() {
		val provider = project.getTasks().register("derk");
		subject = project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder().withComponent(createdUsing(of(Task.class), provider::get)).build()).as(Task.class);
	}

	@Test
	void returnsTaskProviderOnProviderConversion() {
		assertThat(subject.asProvider(), isA(TaskProvider.class));
	}
}
