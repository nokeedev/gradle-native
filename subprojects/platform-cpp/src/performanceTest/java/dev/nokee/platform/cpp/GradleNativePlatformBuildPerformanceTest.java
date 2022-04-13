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

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.platform.cpp.results.DefaultOutputDirSelector;
import dev.nokee.platform.cpp.results.GradleProfilerReporter;
import dev.nokee.platform.cpp.results.PerformanceTestResult;
import dev.nokee.platform.cpp.results.XmlScenarioDataReporter;
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp;
import lombok.SneakyThrows;
import lombok.val;
import net.nokeedev.testing.file.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.nokee.platform.cpp.BuildExperimentExecutor.gradleProfiler;
import static dev.nokee.platform.cpp.BuildExperimentRunner.create;
import static dev.nokee.platform.cpp.RegressionMatchers.hasRegressed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

class GradleNativePlatformBuildPerformanceTest {
	static TestDirectoryProvider testDirectory = new PerformanceTestDirectoryProvider(GradleNativePlatformBuildPerformanceTest.class);

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
				builder.displayName("Software Model");
				builder.outputDirectory(testDirectory.getTestDirectory().resolve("software-model"));
			}))
				.inDirectory(workingDirectory)
				.withWarmUpRuns(5).withInvocationRuns(10)
				.withBuildAction(GradleBuildExperimentActions.runTasks("assemble"))
				.invocation(it -> it.withArguments("--dependency-verification=off"))
				;

			CommandLine.of("git", "clone", "--depth", "1", "https://github.com/gradle/native-platform.git", workingDirectory.toFile()).execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue();
			Files.delete(workingDirectory.resolve("gradle/verification-metadata.xml"));

			current = currentRunner.run();
		}


		if (true) {
			val workingDirectory = perVersionWorkingDirectory(runIndex++);
			final BuildExperimentRunner<GradleInvocationSpec> baselineRunner = create(gradleProfiler(builder -> {
				builder.displayName("Nokee current");
				builder.outputDirectory(testDirectory.getTestDirectory().resolve("nokee-model"));
			}))
				.inDirectory(workingDirectory)
				.withWarmUpRuns(5).withInvocationRuns(10)
				.withBuildAction(GradleBuildExperimentActions.runTasks("assemble"))
				.invocation(it -> it.withArguments("--dependency-verification=off"))
				;

			CommandLine.of("git", "clone", "--depth", "1", "--branch", "nokee-migration", "https://github.com/gradle/native-platform.git", workingDirectory.toFile()).execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue();
			Files.delete(workingDirectory.resolve("gradle/verification-metadata.xml"));
			Files.write(workingDirectory.resolve("buildSrc/build.gradle"), Files.readAllLines(workingDirectory.resolve("buildSrc/build.gradle")).stream().map(it -> {
				if (it.contains("https://repo.nokee.dev/snapshot")) {
					return it.replace("https://repo.nokee.dev/snapshot", System.getProperty("dev.nokee.performance.localRepository.url"));
				} else if (it.contains("dev.nokee:nokee-gradle-plugins")) {
					return "implementation platform('dev.nokee:nokee-gradle-plugins:0.5.0')";
				} else {
					return it;
				}
			}).collect(Collectors.toList()), StandardOpenOption.TRUNCATE_EXISTING);

			baseline = baselineRunner.run();
		}

		results = PerformanceTestResult.builder()
			.testProject("gradle/native-platform").testClass(GradleNativePlatformBuildPerformanceTest.class.getCanonicalName()).testCase("assemble task")
			.experiment("Software Model", baseline)
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

	@Test
	void performanceHistory() {
		// empty test case to ensure performance test was executed
	}

	@Test
	void checkRegression() {
		assertThat(current, not(hasRegressed(baseline)));
	}
}
