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
package dev.nokee.platform.nativebase.internal;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;

@AutoFactory
public final class RegisterCompileTasksPropertyAction extends ModelActionWithInputs.ModelAction2<BinaryIdentifier<?>, ModelState.IsAtLeastRegistered> {
	private final BinaryIdentifier<?> identifier;
	private final ModelRegistry registry;
	private final ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory;

	public RegisterCompileTasksPropertyAction(BinaryIdentifier<?> identifier, @Provided ModelRegistry registry, @Provided ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory) {
		this.identifier = identifier;
		this.registry = registry;
		this.tasksPropertyRegistrationFactory = tasksPropertyRegistrationFactory;
	}

	@Override
	protected void execute(ModelNode entity, BinaryIdentifier<?> identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.equals(this.identifier)) {
			registry.register(tasksPropertyRegistrationFactory.create(ModelPropertyIdentifier.of(identifier, "compileTasks"), SourceCompile.class));
		}
	}
}
