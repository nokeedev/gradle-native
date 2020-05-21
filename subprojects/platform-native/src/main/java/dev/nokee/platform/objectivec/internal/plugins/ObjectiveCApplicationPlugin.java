package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ObjectiveCApplicationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(ObjectiveCApplicationExtension.class, "application", getObjects().newInstance(ObjectiveCApplicationExtension.class));
	}
}
