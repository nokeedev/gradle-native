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
package dev.nokee.platform.base.internal.assembletask;

import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskDescriptionComponent;
import dev.nokee.platform.base.internal.tasks.TaskGroupComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static java.lang.String.format;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class AssembleTaskCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RegisterAssembleLifecycleTaskRule(target.getExtensions().getByType(ModelRegistry.class))));
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new ConfigureAssembleTaskDescriptionRule());
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new ConfigureAssembleTaskGroupRule());
	}

	private static final class ConfigureAssembleTaskDescriptionRule extends ModelActionWithInputs.ModelAction2<AssembleTaskComponent, IdentifierComponent> {
		@Override
		protected void execute(ModelNode entity, AssembleTaskComponent assembleTask, IdentifierComponent identifier) {
			assembleTask.get().addComponent(new TaskDescriptionComponent(format("Assembles the outputs of the %s.", identifier.get())));
		}
	}

	private static final class ConfigureAssembleTaskGroupRule extends ModelActionWithInputs.ModelAction2<AssembleTaskComponent, IdentifierComponent> {
		@Override
		protected void execute(ModelNode entity, AssembleTaskComponent assembleTask, IdentifierComponent identifier) {
			assembleTask.get().addComponent(new TaskGroupComponent(BUILD_GROUP));
		}
	}

	private static final class RegisterAssembleLifecycleTaskRule extends ModelActionWithInputs.ModelAction1<ModelComponentTag<HasAssembleTaskMixIn.Tag>> {
		private final ModelRegistry registry;

		public RegisterAssembleLifecycleTaskRule(ModelRegistry registry) {
			this.registry = registry;
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<HasAssembleTaskMixIn.Tag> ignored1) {
			val task = registry.instantiate(newEntity(ASSEMBLE_TASK_NAME, Task.class, it -> it.ownedBy(entity)));
			entity.addComponent(new AssembleTaskComponent(task));
			ModelStates.register(task);
		}
	}
}
