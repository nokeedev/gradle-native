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
package dev.nokee.ide.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.ide.base.IdeProject;
import org.gradle.api.Buildable;
import org.gradle.api.Describable;
import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import java.util.Set;

public interface IdeProjectInternal extends IdeProject, Describable, Buildable {
	TaskProvider<? extends Task> getGeneratorTask();

	@Internal
	@Override
	default TaskDependency getBuildDependencies() {
		return new TaskDependency() {
			@Override
			public Set<? extends Task> getDependencies(@Nullable Task task) {
				return ImmutableSet.of(getGeneratorTask().get());
			}
		};
	}
}
