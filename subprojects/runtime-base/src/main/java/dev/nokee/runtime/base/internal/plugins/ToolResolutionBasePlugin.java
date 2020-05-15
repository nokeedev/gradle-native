package dev.nokee.runtime.base.internal.plugins;

import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.base.internal.tools.ToolRouteHandler;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;
import static dev.nokee.runtime.base.internal.repositories.NokeeServerService.NOKEE_LOCAL_REPOSITORY_NAME;

public abstract class ToolResolutionBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(FakeMavenRepositoryPlugin.class);

		NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
		parameters.getRouteHandlers().add(ToolRouteHandler.class.getCanonicalName());

		project.getRepositories().withType(MavenArtifactRepository.class).configureEach(repo -> {
			if (NOKEE_LOCAL_REPOSITORY_NAME.equals(repo.getName())) {
				repo.mavenContent(content -> {
					content.includeGroup("dev.nokee.tool");
				});
			}
		});
	}
}
