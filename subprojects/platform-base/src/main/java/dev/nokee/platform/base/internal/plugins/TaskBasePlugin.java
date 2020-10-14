package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.internal.ProjectIdentifier;
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

		val taskViewFactory = new TaskViewFactory(taskRepository, taskConfigurer);
		project.getExtensions().add(TaskViewFactory.class, "__NOKEE_taskViewFactory", taskViewFactory);

		val knownTaskFactory = new KnownTaskFactory(() -> taskRepository, () -> taskConfigurer);
		project.getExtensions().add(KnownTaskFactory.class, "__NOKEE_knownTaskFactory", knownTaskFactory);
	}
}
