package dev.nokee.runtime.darwin.internal.plugins;

import dev.nokee.runtime.base.internal.plugins.ToolResolutionBasePlugin;
import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.darwin.internal.locators.XcodeLocator;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;

public abstract class DarwinToolLocatorSupportPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ToolResolutionBasePlugin.class);

		NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
		parameters.getToolLocators().add(XcodeLocator.class.getCanonicalName());
	}
}
