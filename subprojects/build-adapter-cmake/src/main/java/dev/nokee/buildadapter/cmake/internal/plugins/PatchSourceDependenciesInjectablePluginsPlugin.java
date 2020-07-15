package dev.nokee.buildadapter.cmake.internal.plugins;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.io.IOException;
import java.nio.charset.Charset;

public class PatchSourceDependenciesInjectablePluginsPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("java-gradle-plugin");
		project.getRepositories().gradlePluginPortal();
		project.getRepositories().maven(it -> it.setUrl("https://dl.bintray.com/nokeedev/distributions"));
		project.getRepositories().maven(it -> it.setUrl("https://dl.bintray.com/nokeedev/distributions-snapshots"));
		project.getDependencies().add("implementation", "dev.nokee:nokee-gradle-plugins:" + getVersion());
		val extension = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
		extension.getPlugins().create("redirector", it -> {
			it.setId("dev.nokee.cmake-build-adapter");
			it.setImplementationClass(CmakeBuildAdapterPlugin.class.getCanonicalName());
		});
	}

	@SneakyThrows
	String getVersion() {
		return IOUtils.resourceToString("version.txt", Charset.defaultCharset(), this.getClass().getClassLoader());
	}
}
