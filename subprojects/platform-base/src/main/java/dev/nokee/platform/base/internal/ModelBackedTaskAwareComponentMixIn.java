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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.*;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.util.ConfigureUtil;

public interface ModelBackedTaskAwareComponentMixIn extends TaskAwareComponent {
	@Override
	@SuppressWarnings("unchecked")
	default TaskView<Task> getTasks() {
		return ModelProperties.getProperty(this, "tasks").as(TaskView.class).get();
	}

	@Override
	default void tasks(Action<? super TaskView<Task>> action) {
		action.execute(getTasks());
	}

	@Override
	default void tasks(@SuppressWarnings("rawtypes") Closure closure) {
		tasks(ConfigureUtil.configureUsing(closure));
	}
}
