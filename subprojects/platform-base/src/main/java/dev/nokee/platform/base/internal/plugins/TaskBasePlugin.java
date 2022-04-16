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
package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.*;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TaskBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realizer = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val taskRegistry = new TaskRegistryImpl(ProjectIdentifier.of(project), eventPublisher, project.getTasks());
		project.getExtensions().add(TaskRegistry.class, "__NOKEE_taskRegistry", taskRegistry);

		val taskRepository = new TaskRepository(eventPublisher, realizer, project.getProviders());
		project.getExtensions().add(TaskRepository.class, "__NOKEE_taskRepository", taskRepository);

		val taskConfigurer = new TaskConfigurer(eventPublisher, project.getTasks());
		project.getExtensions().add(TaskConfigurer.class, "__NOKEE_taskConfigurer", taskConfigurer);

		val knownTaskFactory = new KnownTaskFactory(() -> taskRepository, () -> taskConfigurer);
		project.getExtensions().add(KnownTaskFactory.class, "__NOKEE_knownTaskFactory", knownTaskFactory);
	}
}
