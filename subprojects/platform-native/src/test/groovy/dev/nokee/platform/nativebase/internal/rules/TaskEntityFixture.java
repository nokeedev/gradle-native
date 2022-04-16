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

import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRepository;
import org.gradle.api.Project;
import org.gradle.api.Task;

public interface TaskEntityFixture extends NokeeEntitiesFixture {
	Project getProject();

	default TaskRegistry getTaskRegistry() {
		throw new UnsupportedOperationException();
	}

	default TaskRepository getTaskRepository() {
		return new TaskRepository(getEventPublisher(), getEntityRealizer(), getProject().getProviders());
	}

	static TaskIdentifier<Task> aTaskOfVariant(String name, VariantIdentifier<?> owner) {
		return TaskIdentifier.of(TaskName.of(name), Task.class, owner);
	}

	static TaskIdentifier<Task> aTaskOfComponent(String name, ComponentIdentifier owner) {
		return TaskIdentifier.of(TaskName.of(name), Task.class, owner);
	}
}
