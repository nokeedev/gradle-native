package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.platform.cpp.CppApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class CppApplicationPlugin implements Plugin<Project> {

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(CppApplicationExtension.class, "application", getObjects().newInstance(CppApplicationExtension.class));
	}
}
