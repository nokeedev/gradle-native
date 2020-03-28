package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.platform.nativebase.internal.repositories.NokeeServerService;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public abstract class FakeMavenRepositoryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CompatibilityRules.class);
		if (SystemUtils.IS_OS_MAC) {
			Provider<NokeeServerService> service = project.getGradle().getSharedServices().registerIfAbsent("nokeeServer", NokeeServerService.class, it -> {});
			service.get().configure(project.getRepositories());
			service.get().configure(project.getDependencies());
		}
	}
}
