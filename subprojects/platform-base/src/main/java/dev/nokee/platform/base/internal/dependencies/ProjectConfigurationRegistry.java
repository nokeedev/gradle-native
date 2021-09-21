/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.assertConfigured;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.using;

public final class ProjectConfigurationRegistry {
	private final ConfigurationContainer configurationContainer;
	private final ObjectFactory objectFactory;
	private final TaskContainer taskContainer;

	public ProjectConfigurationRegistry(ConfigurationContainer configurationContainer, ObjectFactory objectFactory, TaskContainer taskContainer) {
		this.configurationContainer = configurationContainer;
		this.objectFactory = objectFactory;
		this.taskContainer = taskContainer;
	}

	public static ProjectConfigurationRegistry forProject(Project project) {
		return new ProjectConfigurationRegistry(project.getConfigurations(), project.getObjects(), project.getTasks());
	}

	public Configuration create(String name, Action<? super Configuration> action) {
		return configurationContainer.create(name, using(objectFactory, using(taskContainer, action)));
	}

	public Configuration createIfAbsent(String name, Action<? super Configuration> action) {
		if (hasConfigurationWithName(name)) {
			return assertConfigured(configurationContainer.getByName(name), action);
		}

		return configurationContainer.create(name, using(objectFactory, using(taskContainer, action)));
	}

	// Avoid triggering container rule which realize objects for nothing.
	private boolean hasConfigurationWithName(String name) {
		for (val element : configurationContainer.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
