package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.platform.swift.SwiftApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class SwiftApplicationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(SwiftApplicationExtension.class, "application", getObjects().newInstance(SwiftApplicationExtension.class));
	}
}
