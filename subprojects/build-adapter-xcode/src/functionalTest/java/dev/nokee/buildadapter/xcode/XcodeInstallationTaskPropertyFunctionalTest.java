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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.buildscript.blocks.DependencyNotation;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Syntax;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.buildadapter.xcode.internal.plugins.CurrentXcodeInstallationValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.HasConfigurableXcodeInstallation;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.buildscript;
import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksExecutedAndNotSkipped;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksSkipped;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toNullStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class XcodeInstallationTaskPropertyFunctionalTest {
	GradleRunner executer;
	@TestDirectory Path testDirectory;
	BuildResult result;

	@BeforeEach
	void givenCustomTaskWithCurrentXcodeInstallationConfigured(GradleRunner runner) throws IOException {
		buildscript(it -> it.dependencies(classpath(DependencyNotation.files(runner.getPluginClasspath())))).writeTo(testDirectory.resolve("settings.gradle"));
		Statement.expressionOf(Syntax.groovy(Stream.of("",
			"import " + HasConfigurableXcodeInstallation.class.getCanonicalName(),
			"import " + CurrentXcodeInstallationValueSource.class.getCanonicalName(),
			"",
			"abstract class MyTask extends DefaultTask implements " + HasConfigurableXcodeInstallation.class.getSimpleName() + " {",
			"    @OutputFile",
			"    abstract RegularFileProperty getOutputFile()",
			"    @TaskAction",
			"    void execute() {",
			"        outputFile.get().asFile.text = 'some data'",
			"    }",
			"}",
			"",
			"tasks.register('verify', MyTask) {",
			"    outputFile = project.file('out.txt')",
			"    xcodeInstallation = providers.of(" + CurrentXcodeInstallationValueSource.class.getSimpleName() + ") {}",
			"}"
		).collect(Collectors.joining("\n")))).writeTo(testDirectory.resolve("build.gradle"));
		executer = runner.withArgument("verify");
	}

	@Nested
	class WhenUpToDateBuild {
		@BeforeEach
		void givenBuildIsUpToDate() {
			assertThat(result = executer.build(), tasksExecutedAndNotSkipped(":verify"));
			assertThat(result = executer.build(), tasksSkipped(":verify"));
		}

		@Test
		void skipsTaskWhenXcodeInstallationDoesNotChangesFromPreviousInvocation() {
			result = executer.withEnvironmentVariable("DEVELOPER_DIR", CommandLine.of("xcode-select", "--print-path").newInvocation().redirectStandardOutput(toNullStream()).redirectErrorOutput(toNullStream()).buildAndSubmit(new ProcessBuilderEngine()).waitFor().getOutput().getAsString().trim()).build();
			assertThat(result, tasksSkipped(":verify"));
		}

		@Test
		void doesNotSkipTaskWhenXcodeInstallationChangesFromPreviousInvocation() {
			executer = executer.withEnvironmentVariable("DEVELOPER_DIR", "/Applications/Xcode_13.4.1.app/Contents/Developer");
			assertThat(result = executer.build(), tasksExecutedAndNotSkipped(":verify"));
			assertThat(result = executer.build(), tasksSkipped(":verify"));
		}
	}

	@Nested
	@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
	class WhenReusingConfigurationCache {
		@BeforeEach
		void givenPrimedConfigurationCache() {
			executer = executer.withArgument("--configuration-cache");
			result = executer.build();
			result = executer.build();
			assertThat(result.getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void doesNotInvalidateConfigurationCacheWhenXcodeInstallationChangesToTheSameVersion() {
			// Reuse configuration when DEVELOPER_DIR changes to the same value
			result = executer.withEnvironmentVariable("DEVELOPER_DIR", CommandLine.of("xcode-select", "--print-path").newInvocation().redirectStandardOutput(toNullStream()).redirectErrorOutput(toNullStream()).buildAndSubmit(new ProcessBuilderEngine()).waitFor().getOutput().getAsString().trim()).build();
			assertThat(result.getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void doesNotInvalidateConfigurationCacheWhenXcodeInstallationChangesToDifferentVersion() {
			// Reuse configuration cache when DEVELOPER_DIR changes to a different value
			executer = executer.withEnvironmentVariable("DEVELOPER_DIR", "/Applications/Xcode_13.4.1.app/Contents/Developer");
			result = executer.build();
			assertThat(result.getOutput(), containsString("Reusing configuration cache"));
		}
	}
}
