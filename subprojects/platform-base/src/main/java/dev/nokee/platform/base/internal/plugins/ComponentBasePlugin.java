package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.components.*;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val componentRepository = new ComponentRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(ComponentRepository.class, "__NOKEE_componentRepository", componentRepository);

		val componentConfigurer = new ComponentConfigurer(eventPublisher);
		project.getExtensions().add(ComponentConfigurer.class, "__NOKEE_componentConfigurer", componentConfigurer);

		val knownComponentFactory = new KnownComponentFactory(() -> componentRepository, () -> componentConfigurer);
		project.getExtensions().add(KnownComponentFactory.class, "__NOKEE_knownComponentFactory", knownComponentFactory);

		val componentProviderFactory = new ComponentProviderFactory(componentRepository, componentConfigurer);
		project.getExtensions().add(ComponentProviderFactory.class, "__NOKEE_componentProviderFactory", componentProviderFactory);

		project.getPluginManager().apply("lifecycle-base");
		val extension = new ComponentContainerImpl(ProjectIdentifier.of(project), componentConfigurer, eventPublisher, componentProviderFactory, componentRepository, knownComponentFactory);
		project.getExtensions().add(ComponentContainer.class, "components", extension);
		project.afterEvaluate(proj -> extension.disallowChanges());
	}
}
