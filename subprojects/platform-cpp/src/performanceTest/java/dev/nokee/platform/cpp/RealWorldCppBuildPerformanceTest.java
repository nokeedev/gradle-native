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
package dev.nokee.platform.cpp;

import dev.nokee.platform.cpp.results.DataReporter;
import dev.nokee.platform.cpp.results.DefaultOutputDirSelector;
import dev.nokee.platform.cpp.results.GradleProfilerReporter;
import dev.nokee.platform.cpp.results.PerformanceExperiment;
import dev.nokee.platform.cpp.results.PerformanceScenario;
import dev.nokee.platform.cpp.results.PerformanceTestResult;
import dev.nokee.platform.cpp.results.XmlScenarioDataReporter;
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp;
import lombok.SneakyThrows;
import lombok.val;
import net.nokeedev.testing.file.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.gradle.profiler.flamegraph.DifferentialStacksGenerator;
import org.gradle.profiler.flamegraph.FlameGraphGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.nokee.platform.cpp.BuildExperimentExecutor.gradleProfiler;
import static dev.nokee.platform.cpp.BuildExperimentRunner.create;
import static dev.nokee.platform.cpp.RegressionMatchers.hasRegressed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

class RealWorldCppBuildPerformanceTest {
	static TestDirectoryProvider testDirectory = new PerformanceTestDirectoryProvider(RealWorldCppBuildPerformanceTest.class);

	static BuildExperimentResult current;
	static BuildExperimentResult baseline;
	static PerformanceTestResult results;

	// TODO: Nokee distribution (version GA, nightly, under test)

	@BeforeAll
	static void setup() throws IOException {
		int runIndex = 0;

		if (true) {
			val workingDirectory = perVersionWorkingDirectory(runIndex++);
			final BuildExperimentRunner<GradleInvocationSpec> currentRunner = create(gradleProfiler(builder -> {
				builder.displayName("Nokee v0.4.0");
				builder.outputDirectory(testDirectory.getTestDirectory().resolve("0.4.0"));
			}))
				.inDirectory(workingDirectory)
				.withWarmUpRuns(5).withInvocationRuns(10)
				.withBuildAction(GradleBuildExperimentActions.runTasks("assemble"));

			new CppGreeterApp().writeToProject(workingDirectory.toFile());
			Files.write(workingDirectory.resolve("build.gradle"), Arrays.asList(
				"plugins {",
				"  id 'dev.nokee.cpp-application' version '0.4.0'",
				"}"
			));
			Files.createFile(workingDirectory.resolve("settings.gradle"));

			current = currentRunner.run();
		}


		if (true) {
			val workingDirectory = perVersionWorkingDirectory(runIndex++);
			final BuildExperimentRunner<GradleInvocationSpec> baselineRunner = create(gradleProfiler(builder -> {
				builder.displayName("Nokee current");
				builder.outputDirectory(testDirectory.getTestDirectory().resolve("0.5.0"));
			}))
				.inDirectory(workingDirectory)
				.withWarmUpRuns(5).withInvocationRuns(10)
				.withBuildAction(GradleBuildExperimentActions.runTasks("assemble"));

			new CppGreeterApp().writeToProject(workingDirectory.toFile());
			Files.write(workingDirectory.resolve("build.gradle"), Arrays.asList(
				"plugins {",
				"  id 'dev.nokee.cpp-application' version '0.5.0'",
				"}"
			));
			Files.write(workingDirectory.resolve("settings.gradle"), Arrays.asList(
				"pluginManagement {",
				"  repositories {",
				"    maven { url = '" + System.getProperty("dev.nokee.performance.localRepository.url") + "' }",
				"    gradlePluginPortal()",
				"  }",
				"}"
			));

			baseline = baselineRunner.run();
		}

		results = PerformanceTestResult.builder()
			.testProject("cppApp").testClass(RealWorldCppBuildPerformanceTest.class.getCanonicalName()).testCase("assemble task")
			.experiment("Nokee v0.4.0", baseline)
			.experiment("Nokee current", current)
			.build();
	}

	@SneakyThrows
	private static Path perVersionWorkingDirectory(int runIndex) {
		val versionWorkingDirName = String.format("%03d", runIndex);
		val perVersion = testDirectory.getTestDirectory().resolve(versionWorkingDirName);
		if (!Files.exists(perVersion)) {
			FileUtils.createParentDirectories(perVersion.toFile());
		} else {
			FileUtils.cleanDirectory(perVersion.toFile());
		}
		return perVersion;
	}


	@AfterAll
	static void report() {
		val outputDirSelector = new DefaultOutputDirSelector(testDirectory.getTestDirectory().toFile());
		val gradleProfileReporter = new GradleProfilerReporter(outputDirSelector);

		val reporter = gradleProfileReporter
			.reportAlso(new XmlScenarioDataReporter());
		reporter.report(results);
	}

	///nativeMonolithic

//	def setup() {
//		runner.targetVersions = ["7.4.1-20220211002507+0000"]
//		runner.minimumBaseVersion = "4.0"
//	}

	@Test
	void performanceHistory() {
		// empty test case to ensure performance test was executed
	}

	@Test
	void checkRegression() {
		assertThat(current, not(hasRegressed(baseline)));
	}
}
