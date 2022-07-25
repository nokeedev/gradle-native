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

import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.nativebase.HasCreateTask;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import org.gradle.api.tasks.TaskProvider;

@DomainObjectEntities.Tag(HasCreateTaskMixIn.Tag.class)
public interface HasCreateTaskMixIn extends HasCreateTask {
	default TaskProvider<CreateStaticLibrary> getCreateTask() {
		return (TaskProvider<CreateStaticLibrary>) ModelElements.of(this, NativeArchiveTask.class).as(CreateStaticLibrary.class).asProvider();
	}

	interface Tag extends ModelTag {}
}
