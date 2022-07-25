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
package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.core.ModelComponent;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;

public final class TaskProjectionComponent implements ModelComponent {
	private final NamedDomainObjectProvider<Task> value;

	public TaskProjectionComponent(NamedDomainObjectProvider<Task> value) {
		this.value = value;
	}

	public void configure(Action<? super Task> action) {
		value.configure(action);
	}

	public NamedDomainObjectProvider<Task> get() {
		return value;
	}
}
