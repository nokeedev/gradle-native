package dev.nokee.runtime.base.internal.plugins;

import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.base.internal.tools.ToolRouteHandler;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.Provider;

import static dev.nokee.runtime.base.internal.repositories.NokeeServerService.NOKEE_LOCAL_REPOSITORY_NAME;

public abstract class FakeMavenRepositoryPlugin implements Plugin<Project> {
	public static final String NOKEE_SERVER_SERVICE_NAME = "nokeeServer";

	@Override
	public void apply(Project project) {
		Provider<NokeeServerService> service = project.getGradle().getSharedServices().registerIfAbsent(NOKEE_SERVER_SERVICE_NAME, NokeeServerService.class, it -> {
			it.parameters(p -> p.getRouteHandlers().add(ToolRouteHandler.class.getCanonicalName()));
		});

		project.getRepositories().maven(repo -> {
			repo.setName(NOKEE_LOCAL_REPOSITORY_NAME);
			// It is important to differ the setURL(Provider) as late as possible until the following regression is resolved:
			//  https://github.com/gradle/gradle/issues/13152
			//  NOTE: We can only remove the workaround once we drop support for the affected versions.
			project.afterEvaluate(proj -> {
				repo.setUrl(service.map(NokeeServerService::getUri));
			});
			repo.metadataSources(MavenArtifactRepository.MetadataSources::gradleMetadata);
			repo.mavenContent(content -> {
				content.includeGroup("dev.nokee.tool");
			});
		});
	}
}
