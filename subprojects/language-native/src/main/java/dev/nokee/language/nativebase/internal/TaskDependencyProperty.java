/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.language.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Set;

public abstract class TaskDependencyProperty implements TaskDependency {
	private final SetProperty<TaskDependency> values;

	@Inject
	public TaskDependencyProperty(ObjectFactory objects) {
		this.values = objects.setProperty(TaskDependency.class);
	}

	public void add(TaskDependency element) {
		values.add(element);
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return values.get().stream().flatMap(it -> it.getDependencies(task).stream()).collect(ImmutableSet.toImmutableSet());
	}
}
