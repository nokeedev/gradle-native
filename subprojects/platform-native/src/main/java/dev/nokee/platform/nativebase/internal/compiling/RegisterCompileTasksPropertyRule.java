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
package dev.nokee.platform.nativebase.internal.compiling;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsBinary;

final class RegisterCompileTasksPropertyRule extends ModelActionWithInputs.ModelAction2<IdentifierComponent, ModelComponentTag<IsBinary>> {
	private final ModelRegistry registry;
	private final ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory;

	public RegisterCompileTasksPropertyRule(ModelRegistry registry, ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory) {
		this.registry = registry;
		this.tasksPropertyRegistrationFactory = tasksPropertyRegistrationFactory;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<IsBinary> tag) {
		registry.register(tasksPropertyRegistrationFactory.create("compileTasks", entity, SourceCompile.class));
	}
}
