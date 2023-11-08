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
package dev.nokee.platform.nativebase.internal.archiving;

import dev.nokee.platform.nativebase.HasCreateTask;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;

public interface CreateTaskMixIn extends HasCreateTask, ExtensionAware {
	@SuppressWarnings("unchecked")
	default TaskProvider<CreateStaticLibraryTask> getCreateTask() {
		return (TaskProvider<CreateStaticLibraryTask>) getExtensions().getByName("createTask");
	}
}
