package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.platform.cpp.CppLibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class CppLibraryPlugin implements Plugin<Project> {

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(CppLibraryExtension.class, "library", getObjects().newInstance(CppLibraryExtension.class));
	}
}
