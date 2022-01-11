/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nokeebuild;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.inject.Inject;

import static nokeebuild.ConfigureMinimumSupportedGradle.minimumGradleVersion;
import static nokeebuild.GradleDevelopmentUtils.gradlePlugin;
import static nokeebuild.GradleDevelopmentUtils.java;

abstract /*final*/ class JavaGradleLibraryDevelopmentPlugin implements Plugin<Project> {
	@Inject
	public JavaGradleLibraryDevelopmentPlugin() {}

	@Override
	public void apply(Project project) {
//		project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
//		project.getPluginManager().apply("java-gradle-plugin");
		project.getPluginManager().apply("nokeebuild.jvm-base");
		project.getPluginManager().apply("dev.gradleplugins.java-gradle-plugin");
		gradlePlugin(project, disallowPluginsRegistration());
		gradlePlugin(project, new ConfigureMinimumSupportedGradle(minimumGradleVersion(project)));
		java(project, JavaPluginExtension::withJavadocJar);
		java(project, JavaPluginExtension::withSourcesJar);
	}

	private static Action<GradlePluginDevelopmentExtension> disallowPluginsRegistration() {
		return extension -> extension.getPlugins().configureEach(it -> { throw new UnsupportedOperationException(); });
	}
}
