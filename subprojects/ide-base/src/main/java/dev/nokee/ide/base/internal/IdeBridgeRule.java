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

import org.gradle.api.Describable;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;

public abstract class IdeBridgeRule<T extends IdeRequest> implements Rule {
	private final Describable ide;

	public IdeBridgeRule(Describable ide) {
		this.ide = ide;
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public String getDescription() {
		return String.format("%s IDE bridge tasks begin with %s. Do not call these directly.", ide.getDisplayName(), getLifecycleTaskNamePrefix());
	}

	protected abstract String getLifecycleTaskNamePrefix();

	@Override
	public void apply(String taskName) {
		if (taskName.startsWith(getLifecycleTaskNamePrefix())) {
			T request = newRequest(taskName);
			if (request.getAction().equals(IdeRequestAction.CLEAN)) {
				Task bridgeTask = getTasks().create(request.getTaskName());
				bridgeTask.dependsOn(LifecycleBasePlugin.CLEAN_TASK_NAME);
			} else {
				doHandle(request);
			}
		}
	}

	public abstract T newRequest(String taskName);

	protected abstract void doHandle(T request);
}
