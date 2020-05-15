package dev.nokee.runtime.darwin.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class DarwinRuntimePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(DarwinToolLocatorSupportPlugin.class);
		project.getPluginManager().apply(DarwinFrameworkResolutionSupportPlugin.class);
	}
}
