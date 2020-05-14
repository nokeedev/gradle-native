package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.platform.nativebase.internal.repositories.NokeeServerService;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.Provider;

import static dev.nokee.platform.nativebase.internal.repositories.NokeeServerService.NOKEE_LOCAL_REPOSITORY_NAME;

public abstract class FakeMavenRepositoryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CompatibilityRules.class);
		if (SystemUtils.IS_OS_MAC) {
			Provider<NokeeServerService> service = project.getGradle().getSharedServices().registerIfAbsent("nokeeServer", NokeeServerService.class, it -> {});

			project.getRepositories().maven(repo -> {
				repo.setName(NOKEE_LOCAL_REPOSITORY_NAME);
				repo.setUrl(service.map(NokeeServerService::getUri));
				repo.metadataSources(MavenArtifactRepository.MetadataSources::gradleMetadata);
				repo.mavenContent(content -> {
					content.includeGroup("dev.nokee.tool");
				});
			});
		}
	}
}
