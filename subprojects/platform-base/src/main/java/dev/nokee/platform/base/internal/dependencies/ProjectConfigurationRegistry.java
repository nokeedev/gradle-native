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
