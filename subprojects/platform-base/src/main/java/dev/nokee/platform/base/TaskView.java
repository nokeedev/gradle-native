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
package dev.nokee.platform.base;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;

/**
 * A view of the tasks that are created and configured as they are required.
 *
 * @param <T> type of the tasks in this view
 * @since 0.3
 */
public interface TaskView<T extends Task> extends View<T>, ComponentTasks {
	/**
	 * Returns a task view containing the objects in this view of the given type.
	 * The returned view is live, so that when matching objects are later added to this view, they are also visible in the filtered task view.
	 *
	 * @param type The type of task to find.
	 * @param <S> The base type of the new task view.
	 * @return the matching element as a {@link TaskView}, never null.
	 */
	<S extends T> TaskView<S> withType(Class<S> type);

	<S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type);
}
