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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.gradle.api.Task;

import static dev.nokee.utils.TaskUtils.configureDescription;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public final class RegisterAssembleLifecycleTaskRule extends ModelActionWithInputs.ModelAction2<ModelProjection, IdentifierComponent> {
	private final TaskRegistrationFactory taskRegistrationFactory;
	private final ModelRegistry registry;

	public RegisterAssembleLifecycleTaskRule(TaskRegistrationFactory taskRegistrationFactory, ModelRegistry registry) {
		super(ModelComponentReference.ofProjection(ModelBackedHasAssembleTaskMixIn.class), ModelComponentReference.of(IdentifierComponent.class));
		this.taskRegistrationFactory = taskRegistrationFactory;
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection tag, IdentifierComponent identifier) {
		val taskIdentifier = TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier.get());
		val task = registry.instantiate(taskRegistrationFactory.create(taskIdentifier, Task.class).build());
		registry.instantiate(ModelAction.configure(task.getId(), Task.class, configureGroup(BUILD_GROUP)));
		registry.instantiate(ModelAction.configure(task.getId(), Task.class, configureDescription("Assembles the outputs of the %s.", identifier.get())));

		entity.addComponent(new AssembleTaskComponent(task));

		ModelStates.register(task);
	}
}
