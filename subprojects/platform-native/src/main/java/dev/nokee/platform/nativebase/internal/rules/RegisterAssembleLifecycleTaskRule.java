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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.DeferUtils;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import java.util.Arrays;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public final class RegisterAssembleLifecycleTaskRule extends ModelActionWithInputs.ModelAction3<IdentifierComponent, IsComponent, ModelState.IsAtLeastRegistered> {
	private final ComponentIdentifier identifier;
	private final PolymorphicDomainObjectRegistry<Task> taskRegistry;
	private final ModelRegistry modelRegistry;
	private final ProviderFactory providers;

	public RegisterAssembleLifecycleTaskRule(ComponentIdentifier identifier, PolymorphicDomainObjectRegistry<Task> taskRegistry, ModelRegistry modelRegistry, ProviderFactory providers) {
		this.identifier = identifier;
		this.taskRegistry = taskRegistry;
		this.modelRegistry = modelRegistry;
		this.providers = providers;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, IsComponent tag, ModelState.IsAtLeastRegistered stateTag) {
		if (!this.identifier.equals(identifier.get())) {
			return;
		}

		// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
		//   then we configure the dependency.
		//   Note that the dependency may already exists for single variant component but it's not a big deal.
		@SuppressWarnings("unchecked")
		final Provider<HasDevelopmentVariant<?>> component = providers.provider(() -> ModelNodeUtils.get(entity, HasDevelopmentVariant.class));
		Provider<? extends Variant> developmentVariant = component.flatMap(HasDevelopmentVariant::getDevelopmentVariant);
		val logger = new WarnUnbuildableLogger((ComponentIdentifier) identifier.get());
		val taskIdentifier = TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier.get());
		val taskProvider = (TaskProvider<Task>) taskRegistry.registerIfAbsent(taskIdentifier.getTaskName(), Task.class);
		val task = modelRegistry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(taskIdentifier))
				.withComponent(IsTask.tag())
				.withComponent(ConfigurableTag.tag())
				.withComponent(createdUsing(ModelType.of(Task.class), taskProvider::get))
				.withComponent(createdUsing(ModelType.of(TaskProvider.class), () -> taskProvider))
				.build()).as(Task.class);
		task.configure(configureGroup(BUILD_GROUP));
		task.configure(configureDependsOn(developmentVariant.flatMap(TO_DEVELOPMENT_BINARY).map(Arrays::asList)
			.orElse(DeferUtils.executes(onlyOnce(logger::warn)))));
		task.configure(configureDescription("Assembles the outputs of the %s.", identifier.get()));

		taskProvider.configure(it -> ModelStates.realize(ModelNodes.of(task)));
	}
}
