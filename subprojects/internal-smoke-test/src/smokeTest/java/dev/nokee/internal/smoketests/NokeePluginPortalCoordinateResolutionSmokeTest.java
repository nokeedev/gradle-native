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
package dev.nokee.internal.smoketests;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import lombok.val;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatements.gradlePluginPortal;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class NokeePluginPortalCoordinateResolutionSmokeTest {
	@TempDir
	Path testDirectory;

	@ParameterizedTest(name = "can use Gradle module metadata during resolution from plugin.gradle.org/m2 [{arguments}]")
	@ValueSource(strings = {"0.3.0", "0.4.0"}) // only since 0.3.0
	void canUseGradleModuleMetadataDuringResolution(String nokeeVersion) throws IOException {
		GradleBuildFile.inDirectory(testDirectory).configure(createProject(nokeeVersion));
		val result = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory)
			.withArguments("dependencyInsight", "--configuration", "pluginClasspath", "--dependency",
				"dev.nokee:platformJni:" + nokeeVersion, "--refresh-dependencies")
			.build();
		assertThat(result.task(":dependencyInsight").getOutput(), containsString(String.join("\n",
			"dev.nokee:platformJni:" + nokeeVersion,
				"   variant \"runtimeElements\" [",
				"      org.gradle.category            = library (not requested)",
				"      org.gradle.dependency.bundling = external (not requested)",
				"      org.gradle.jvm.version         = 8 (not requested)",
				"      org.gradle.libraryelements     = jar (not requested)",
				"      org.gradle.usage               = java-runtime (not requested)",
				"      org.gradle.status              = release (not requested)",
				"   ]",
				"",
				"dev.nokee:platformJni:" + nokeeVersion,
				"\\--- pluginClasspath"
			)));
	}

	static Consumer<ProjectBlock> createProject(String version) {
		return project -> project
			.repositories(it -> it.add(gradlePluginPortal()))
			.configurations(it -> it.add(groovyDsl(
				"pluginClasspath {",
				"  canBeConsumed = false",
				"  canBeResolved = true",
				"}")))
			.dependencies(it -> it.add("pluginClasspath", "dev.nokee:platformJni:" + version));
	}
}
