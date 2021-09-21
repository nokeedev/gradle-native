package dev.nokeebuild.licensing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract /*final*/ class NokeeLicensePlugin implements Plugin<Project> {
	@Inject
	public NokeeLicensePlugin() {}

	@Override
	public void apply(Project project) {
		project.getExtensions().create(LicenseExtension.class, "license", NokeeLicenseExtension.class);
		project.getPluginManager().apply(LicenseBasePlugin.class);
	}
}
