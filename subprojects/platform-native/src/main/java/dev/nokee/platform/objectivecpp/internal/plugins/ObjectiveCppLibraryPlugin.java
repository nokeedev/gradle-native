package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(ObjectiveCppLibraryExtension.class, "library", getObjects().newInstance(ObjectiveCppLibraryExtension.class));
	}
}
