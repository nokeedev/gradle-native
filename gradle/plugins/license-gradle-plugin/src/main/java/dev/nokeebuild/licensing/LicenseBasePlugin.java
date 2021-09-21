package dev.nokeebuild.licensing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract /*final*/ class LicenseBasePlugin implements Plugin<Project> {
	@Inject
	public LicenseBasePlugin() {}

	@Override
	public void apply(Project project) {
		if (isRootProject(project)) {
			project.getPluginManager().withPlugin("org.jetbrains.gradle.plugin.idea-ext",
				new ConfigureIdeaCopyrightProfileAction(GradleExtensions.from(project)));
		}
		project.getPluginManager().withPlugin("com.diffplug.spotless",
			new ConfigureSpotlessLicenseHeaderAction(GradleExtensions.from(project), project.getPluginManager()));
	}

	private static boolean isRootProject(Project project) {
		return project.getParent() == null;
	}
}
