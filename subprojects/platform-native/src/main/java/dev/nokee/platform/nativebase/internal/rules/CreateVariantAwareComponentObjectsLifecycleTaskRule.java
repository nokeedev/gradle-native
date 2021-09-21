/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import org.gradle.api.Action;

import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateVariantAwareComponentObjectsLifecycleTaskRule implements Action<VariantAwareComponentInternal<?>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantAwareComponentObjectsLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(VariantAwareComponentInternal<?> component) {
		taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("objects"), ObjectsLifecycleTask.class, component.getIdentifier())).configure(configureDependsOn(component.getDevelopmentVariant().flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS)));
	}
}
