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
package dev.nokee.language.jvm.internal;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import org.gradle.api.Task;

@AutoFactory
public final class JvmCompileTaskRegistrationAction extends ModelActionWithInputs.ModelAction3<IdentifierComponent, IsLanguageSourceSet, ModelState.IsAtLeastRegistered> {
	private final LanguageSourceSetIdentifier identifier;
	private final Class<? extends Task> compileTaskType;
	private final ModelRegistry registry;
	private final TaskRegistrationFactory taskRegistrationFactory;
	private final ModelPropertyRegistrationFactory propertyRegistrationFactory;

	public <T extends SourceCompile> JvmCompileTaskRegistrationAction(LanguageSourceSetIdentifier identifier, Class<? extends Task> compileTaskType, @Provided ModelRegistry registry, @Provided TaskRegistrationFactory taskRegistrationFactory, @Provided ModelPropertyRegistrationFactory propertyRegistrationFactory) {
		this.identifier = identifier;
		this.compileTaskType = compileTaskType;
		this.registry = registry;
		this.taskRegistrationFactory = taskRegistrationFactory;
		this.propertyRegistrationFactory = propertyRegistrationFactory;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, IsLanguageSourceSet tag, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.get().equals(this.identifier)) {
			registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), compileTaskType, identifier.get()), compileTaskType).build());
		}
	}
}
