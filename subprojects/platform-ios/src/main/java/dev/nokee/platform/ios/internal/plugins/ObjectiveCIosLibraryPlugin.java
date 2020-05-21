package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.ios.ObjectiveCIosLibraryExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ObjectiveCIosLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(ObjectiveCIosLibraryExtension.class, "library", getObjects().newInstance(DefaultObjectiveCIosLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class)));
	}
}
