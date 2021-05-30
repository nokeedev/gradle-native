package dev.nokee.model.plugins;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.dsl.ModelNode;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.model.internal.registry.ModelRegistry;

import javax.inject.Inject;

public abstract class ProjectPlugin extends ModelPlugin<Project> {
	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract DependencyHandler getDependencies();

	protected final ModelRegistry getModelRegistry() {
		return ((ProjectInternal) target).getModelRegistry();
	}

	@Override
	protected abstract void apply(ModelNode node, KnownDomainObject<Project> knownProject);
}
