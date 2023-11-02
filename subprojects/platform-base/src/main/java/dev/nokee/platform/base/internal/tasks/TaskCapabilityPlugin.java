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
package dev.nokee.platform.base.internal.tasks;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementConfigurableProviderSourceComponent;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.IsTask;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.TaskUtils.configureGroup;

public class TaskCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final TaskContainer tasks;

	@Inject
	public TaskCapabilityPlugin(TaskContainer tasks) {
		this.tasks = tasks;
	}

	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new SyncDescriptionToTaskProjectionRule());
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new SyncGroupToTaskProjectionRule());

		tasks.configureEach(task -> {
			target.getExtensions().getByType(ModelLookup.class).query(entity -> entity.find(TaskProjectionComponent.class).map(it -> it.get().getName()).map(task.getName()::equals).orElse(false)).forEach(ModelStates::realize);
		});

		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsTask.class), ModelComponentReference.of(TaskTypeComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), ModelComponentReference.of(IdentifierComponent.class), (entity, ignored1, implementationType, fullyQualifiedName, identifier) -> {
			val taskRegistry = model(target, registryOf(Task.class));
			MutationGuards.of(tasks).withMutationEnabled(__ -> {
				val taskProvider = (TaskProvider<Task>) taskRegistry.register(identifier.get(), implementationType.get()).asProvider();
				entity.addComponent(new ModelElementProviderSourceComponent(taskProvider));
				entity.addComponent(createdUsingNoInject(ModelType.of(implementationType.get()), taskProvider::get));
				entity.addComponent(createdUsing(objectProviderOf(implementationType.get()), () -> taskProvider));
				entity.addComponent(new TaskProjectionComponent(taskProvider));
				entity.addComponent(new ModelElementConfigurableProviderSourceComponent(taskProvider));
			}).execute(null);
		}));
	}

	@SuppressWarnings("unchecked")
	private static <S extends Task> ModelType<TaskProvider<S>> objectProviderOf(Class<S> type) {
		return (ModelType<TaskProvider<S>>) ModelType.of(new TypeToken<TaskProvider<S>>() {}.where(new TypeParameter<S>() {}, type).getType());
	}

	private static final class SyncDescriptionToTaskProjectionRule extends ModelActionWithInputs.ModelAction2<TaskDescriptionComponent, TaskProjectionComponent> {
		@Override
		protected void execute(ModelNode entity, TaskDescriptionComponent description, TaskProjectionComponent projection) {
			projection.configure(task -> {
				// FIXME: Should check if its single/multi variants
				if (task.getDescription() == null) {
					task.setDescription(description.get());
				}
			});
		}
	}

	private static final class SyncGroupToTaskProjectionRule extends ModelActionWithInputs.ModelAction2<TaskGroupComponent, TaskProjectionComponent> {
		@Override
		protected void execute(ModelNode entity, TaskGroupComponent group, TaskProjectionComponent task) {
			task.configure(configureGroup(group.get()));
		}
	}
}
