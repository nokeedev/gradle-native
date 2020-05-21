package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;
import dev.nokee.platform.objectivec.internal.DefaultObjectiveCLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ObjectiveCLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(ObjectiveCLibraryExtension.class, "library", getObjects().newInstance(DefaultObjectiveCLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class)));
	}
}
