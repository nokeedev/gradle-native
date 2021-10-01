/*
 * Copyright 2021 the original author or authors.
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
package dev.gradleplugins.grava.testing;

import dev.nokee.internal.testing.WellBehavedPluginTester;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DetectResolvedConfigurationWellBehavedPluginTesterIntegrationTest {
	@Test
	void failsIfProjectPluginResolveConfiguration() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(ResolveConfigurationProjectPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Project).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not resolve configuration")));
	}

	@Test
	void failsIfSettingsPluginResolveConfiguration() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(ResolveConfigurationSettingsPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Settings).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not resolve configuration")));
	}

	@Test
	void failsIfInitPluginResolveConfiguration() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(ResolveConfigurationInitPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Init).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not resolve configuration")));
	}

	@Test
	void failsIfPluginResolveConfigurationInSubprojects() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(ResolveSubprojectsConfigurationPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Init).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not resolve configuration")));
	}

	private static String createDependencyToResolve(File repositoryDirectory) {
		val pom = new File(repositoryDirectory, "com/example/foo/4.2/foo-4.2.pom");
		pom.getParentFile().mkdirs();
		try {
			Files.write(pom.toPath(), Stream.of(
				"<project>",
				"  <modelVersion>4.0.0</modelVersion>",
				"  <groupId>com.example</groupId>",
				"  <artifactId>foo</artifactId>",
				"  <version>4.2</version>",
				"</project>"
			).collect(Collectors.toList()), UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return "com.example:foo:4.2@pom";
	}

	private static void resolveConfiguration(Project project) {
		val repoDir = project.file("repo");
		project.getRepositories().maven(it -> it.setUrl(repoDir));
		val configuration = project.getConfigurations().create("foo");
		project.getDependencies().add(configuration.getName(), createDependencyToResolve(repoDir));
		configuration.resolve();
	}

	public static class ResolveSubprojectsConfigurationPlugin implements Plugin<Project> {
		@Override
		public void apply(Project project) {
			project.subprojects(DetectResolvedConfigurationWellBehavedPluginTesterIntegrationTest::resolveConfiguration);
		}
	}

	public static class ResolveConfigurationProjectPlugin implements Plugin<Project> {
		@Override
		public void apply(Project project) {
			project.getGradle().rootProject(DetectResolvedConfigurationWellBehavedPluginTesterIntegrationTest::resolveConfiguration);
		}
	}

	public static class ResolveConfigurationSettingsPlugin implements Plugin<Settings> {
		@Override
		public void apply(Settings settings) {
			settings.getGradle().rootProject(DetectResolvedConfigurationWellBehavedPluginTesterIntegrationTest::resolveConfiguration);
		}
	}

	public static class ResolveConfigurationInitPlugin implements Plugin<Gradle> {
		@Override
		public void apply(Gradle gradle) {
			gradle.rootProject(DetectResolvedConfigurationWellBehavedPluginTesterIntegrationTest::resolveConfiguration);
		}
	}
}
