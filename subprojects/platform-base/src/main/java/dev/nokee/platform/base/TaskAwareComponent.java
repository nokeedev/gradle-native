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
package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Task;

/**
 * A component with tasks.
 *
 * @since 0.5
 */
public interface TaskAwareComponent {
	/**
	 * Returns the component tasks of this component.
	 *
	 * @return the component tasks of this component, never null
	 */
	TaskView<Task> getTasks();

	/**
	 * Configures the component tasks using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 */
	void tasks(Action<? super TaskView<Task>> action);
	void tasks(@DelegatesTo(value = TaskView.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}
