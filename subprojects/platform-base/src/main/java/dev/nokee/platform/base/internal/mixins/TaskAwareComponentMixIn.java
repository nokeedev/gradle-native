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
package dev.nokee.platform.base.internal.mixins;

import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.decorators.Decorate;
import dev.nokee.platform.base.TaskAwareComponent;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.NestedViewDecorator;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;

public interface TaskAwareComponentMixIn extends ModelMixIn, TaskAwareComponent {
	@Override
	@Decorate(NestedViewDecorator.class)
	default TaskView<Task> getTasks() {
		return mixedIn("tasks");
	}

	@Override
	default void tasks(Action<? super TaskView<Task>> action) {
		action.execute(getTasks());
	}

	@Override
	default void tasks(@SuppressWarnings("rawtypes") Closure closure) {
		tasks(new ClosureWrappedConfigureAction<>(closure));
	}
}
