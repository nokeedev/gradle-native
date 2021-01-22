package dev.nokee.platform.base.internal.dependencies;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.assertConfigured;

public final class ProjectConfigurationRegistry {
	private final ConfigurationContainer configurationContainer;
	private final ObjectFactory objectFactory;

	public ProjectConfigurationRegistry(ConfigurationContainer configurationContainer, ObjectFactory objectFactory) {
		this.configurationContainer = configurationContainer;
		this.objectFactory = objectFactory;
	}

	public static ProjectConfigurationRegistry forProject(Project project) {
		return new ProjectConfigurationRegistry(project.getConfigurations(), project.getObjects());
	}

	public Configuration createIfAbsent(String name, Action<? super Configuration> action) {
		if (hasConfigurationWithName(name)) {
			return assertConfigured(configurationContainer.getByName(name), action);
		}

		return configurationContainer.create(name, ProjectConfigurationActions.using(objectFactory, action));
	}

	public NamedDomainObjectProvider<Configuration> registerIfAbsent(String name, Action<? super Configuration> action) {
		if (hasConfigurationWithName(name)) {
			return configurationContainer.named(name, configuration -> assertConfigured(configuration, action));
		}

		return configurationContainer.register(name, ProjectConfigurationActions.using(objectFactory, action));
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
