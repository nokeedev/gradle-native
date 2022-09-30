/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.docs.samples;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dev.gradleplugins.exemplarkit.Exemplar;
import dev.gradleplugins.exemplarkit.ExemplarExecutionResult;
import dev.gradleplugins.exemplarkit.ExemplarExecutor;
import dev.gradleplugins.exemplarkit.ExemplarRunner;
import dev.gradleplugins.exemplarkit.Step;
import dev.gradleplugins.exemplarkit.StepExecutionContext;
import dev.gradleplugins.exemplarkit.StepExecutionOutcome;
import dev.gradleplugins.exemplarkit.StepExecutionResult;
import dev.gradleplugins.exemplarkit.StepExecutor;
import dev.gradleplugins.exemplarkit.output.JarCommandOutput;
import dev.gradleplugins.exemplarkit.output.TreeCommandOutput;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutput;
import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains;
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.docs.fixtures.OnlyIfCondition;
import dev.nokee.docs.fixtures.SampleContentFixture;
import dev.nokee.docs.fixtures.SampleUnderTestExtension;
import net.nokeedev.testing.file.TestDirectoryProvider;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.internal.os.OperatingSystem;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.exemplarkit.StepExecutionResult.stepExecuted;
import static dev.gradleplugins.exemplarkit.StepExecutors.replaceIfAbsent;
import static dev.gradleplugins.exemplarkit.StepExecutors.skipIf;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toSystemError;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toSystemOutput;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assume.assumeThat;

@ExtendWith({TestDirectoryExtension.class, SampleUnderTestExtension.class})
public abstract class WellBehavingSampleTest {
	@TestDirectory public TestDirectoryProvider temporaryFolder;

	SampleContentFixture fixture;

	@BeforeEach
	void setup(SampleContentFixture fixture) {
		this.fixture = fixture;
	}

	protected GradleRunner configureLocalPluginResolution(GradleRunner runner) {
		try {
			Path initScriptFile = temporaryFolder.getTestDirectory().resolve("repo.init.gradle");
			Files.write(initScriptFile, Arrays.asList("",
				"beforeSettings { settings ->",
				"	settings.pluginManagement {",
				"		repositories {",
				"			gradlePluginPortal()",
				"			maven {",
				"				name = 'docs'",
				"				url = '" + new File(System.getProperty("dev.nokee.docsRepository")).toURI() + "'",
				"			}",
				"		}",
				"	}",
				"}"));
			return runner.usingInitScript(initScriptFile.toFile());
		} catch (IOException e) {
			throw new org.jsoup.UncheckedIOException(e);
		}
	}

	protected TestFile getTestDirectory() {
		return TestFile.of(temporaryFolder.getTestDirectory().toFile());
	}

	// TODO: Migrate to TestFile
	protected static String unzipTo(File zipFile, File workingDirectory) {
		try {
			assertThat(zipFile, anExistingFile());
			workingDirectory.mkdirs();
			CommandLineToolExecutionResult result = CommandLineTool.of("unzip")
				.withArguments(zipFile.getCanonicalPath(), "-d", workingDirectory.getCanonicalPath())
				.newInvocation()
				.redirectStandardOutput(toSystemOutput())
				.redirectErrorOutput(toSystemError())
				.workingDirectory(workingDirectory)
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor(Duration.ofSeconds(30))
				.assertNormalExitValue();

			return result.getStandardOutput().getAsString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@ParameterizedTest(name = "ensure root project name is configured for the sample [{0}]")
	@EnumSource(GradleScriptDsl.class)
	void ensureRootProjectNameIsConfiguredForTheSample(GradleScriptDsl dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.getTestDirectory().toFile());

//		expect:
		// TODO: Improve assertion to ensure it's rootProject.name = <sampleName> and not just a random <sampleName> in the settings script
		assertThat(getTestDirectory().file(dsl.getSettingsFileName()).assertIsFile().getText(), containsString(fixture.getSampleName()));
	}

	@Test
	void ensureSampleHasACategory() {
		assertThat(fixture.getCategory(), notNullValue());
	}

	@Test
	void ensureSampleHasAValidSummary() {
		assertThat(fixture.getSummary(), notNullValue());
		assertThat("is a complete sentence", fixture.getSummary(), endsWith("."));
	}

	public List<String> getExpectedAdditionalExtensions() {
		return Collections.emptyList();
	}

//	@ParameterizedTest(name = "ensure sample source files matches source layout [{0}]")
//	@EnumSource(GradleScriptDsl.class)
//	void ensureSampleSourceFilesMatchesSourceLayout(GradleScriptDsl dsl) {
//		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.getTestDirectory().toFile());
//
//		Map<String, List<String>> pluginIdsToExtensions = ImmutableMap.<String, List<String>>builder()
//			.put("dev.nokee.c", ImmutableList.of("c"))
//			.put("dev.nokee.cpp", ImmutableList.of("cp", "cpp", "c++", "cc", "cxx"))
//			.put("dev.nokee.objective-c", ImmutableList.of("m"))
//			.put("dev.nokee.objective-cpp", ImmutableList.of("mm"))
//			.put("dev.nokee.swift", ImmutableList.of("swift"))
//			.put("dev.gradleplugins.java", ImmutableList.of("java"))
//			.put("dev.gradleplugins.groovy", ImmutableList.of("groovy"))
//			.put("java", ImmutableList.of("java"))
//			.put("groovy", ImmutableList.of("groovy"))
//			.put("org.jetbrains.kotlin.jvm", ImmutableList.of("kt"))
//			.put("cpp", ImmutableList.of("cpp"))
//			.build();
//
//		def languageExtensions = pluginIdsToExtensions.values().flatten()
//		def allBuildFiles = []
//		def allFilesByExtensions = [:].withDefault { [] }
//		testDirectory.eachFileRecurse(FileType.FILES) {
//			if (GradleScriptDsl.values()*.buildFileName.contains(it.name)) {
//				allBuildFiles.add(it)
//			} else if (languageExtensions.contains(getExtension(it.name))) {
//				allFilesByExtensions.get(getExtension(it.name)).add(it)
//			}
//		}
//
//		def allAppliedPlugins = allBuildFiles.collect {
//			def m = (it.text =~ /id[( ]["'](.+?)['"]\)?/)
//			def result = []
//			for (i in 0..<m.count) {
//				result.add(m[i][1])
//			}
//			return result
//		}.flatten()
//
//		println "Found files: ${allFilesByExtensions}"
//		println "Found applied plugins: ${allAppliedPlugins}"
//
//		pluginIdsToExtensions.retainAll { k, v -> allAppliedPlugins.any { it.startsWith(k) } }
//		def effectiveLanguageExtensions = pluginIdsToExtensions.values().flatten() + expectedAdditionalExtensions
//		println "Effective language extensions: ${effectiveLanguageExtensions}"
//
//		allFilesByExtensions.removeAll { k, v -> effectiveLanguageExtensions.contains(k) }
//		println "Left over files: ${allFilesByExtensions}"
//
////		expect:
//		assertThat(allFilesByExtensions, anEmptyMap());
//	}

	/**
	 * Timing values are heavily dependent on the system where the command was executed.
	 * It's better to remove the information to avoid creating false or misleading expectation on the performance.
	 */
	@Test
	void ensureGradleCommandsDoesNotHaveAnyTimingValuesInBuildResult() {
		// TODO: Reports all the error at once instead of failing on the first one
		List<Step> gradleSteps = fixture.getDslExemplar(GradleScriptDsl.GROOVY_DSL).getSteps().stream().filter(it -> it.getExecutable().endsWith("gradlew")).collect(Collectors.toList());
		assumeThat("Gradle commands are present", gradleSteps.size(), greaterThan(0));

		gradleSteps.forEach(it -> assertNoTimingInformationOnBuildResult(it));
	}

	private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*");
	private static void assertNoTimingInformationOnBuildResult(Step step) {
		step.getOutput().ifPresent(output -> assertThat(output, not(matchesPattern(BUILD_RESULT_PATTERN))));
	}

//	@Disabled // sample player is deactivated
//	@Tag("Baked")
//	def "has the twitter player meta data"() {
////		expect:
//		def it = fixture.bakedFile
//		def twitterImages = it.findAll(HtmlTag.META).findAll { it.twitterImage }
//		assertThat("${it.uri} does not have the right meta twitter image tag count", twitterImages, iterableWithSize(1))
//		assertThat(twitterImages.first().content, equalTo("${it.canonicalPath}all-commands.png"));
//
////		and:
//		def twitterCards = it.findAll(HtmlTag.META).findAll { it.twitterCard }
//		assertThat("${it.uri} does not have the right meta twitter card tag count", twitterCards, iterableWithSize(1))
//		assertThat(twitterCards.first().content, equalTo("player"));
//
////		and:
//		def twitterPlayers = it.findAll(HtmlTag.META).findAll { it.twitterPlayer }
//		assertThat("${it.uri} does not have the right meta twitter player tag count", twitterPlayers, iterableWithSize(1))
//		assertThat(twitterPlayers.first().content, equalTo("${it.canonicalPath}all-commands.embed.html"));
//	}

	@ParameterizedTest(name = "can run ./gradlew {0} successfully [{1}]")
	@MethodSource("provideTaskNameWithDsl")
	void canRunGradleForSpecifiedTaskNameSuccessfully(String taskName, GradleScriptDsl dsl) throws IOException {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.getTestDirectory().toFile());

		GradleRunner executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(getTestDirectory()));

		executer.withTasks(taskName).build();
	}

	static Stream<Arguments> provideTaskNameWithDsl() {
		return Sets.cartesianProduct(ImmutableSet.of("help", "tasks"), ImmutableSet.of(GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL)).stream().map(it -> Arguments.of(it.toArray(new Object[0])));
	}

	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.AVAILABLE;
	}

	AvailableToolChains.InstalledToolChain toolChain;
	@ParameterizedTest(name = "can execute commands successfully [{0}]")
	@EnumSource(GradleScriptDsl.class)
	void canExecuteCommandsSuccessfully(GradleScriptDsl dsl) {
		System.out.println("Sample under test directory: " + temporaryFolder.getTestDirectory());
		toolChain = AvailableToolChains.getToolChain(getToolChainRequirement());
		Assumptions.assumeTrue(toolChain != null && toolChain.meets(ToolChainRequirement.AVAILABLE));

		Exemplar exemplar = fixture.getDslExemplar(dsl);
		ExemplarExecutor executor = ExemplarExecutor.builder()
			.registerCommandLineToolExecutor(skipIf(it -> !OnlyIfCondition.of(Objects.toString(it.getAttributes().get("only-if"), null)).canExecute()))
			.registerCommandLineToolExecutor(new JarStepExecutor())
			.registerCommandLineToolExecutor(skipIf(it -> it.getExecutable().equals("tree") && IS_OS_WINDOWS))
			.registerCommandLineToolExecutor(new UnzipStepExecutor())
			.registerCommandLineToolExecutor(skipIf(it -> it.getExecutable().equals("ls") && IS_OS_WINDOWS))
			.registerCommandLineToolExecutor(replaceIfAbsent("mv", "move"))
			.registerCommandLineToolExecutor(new GradleWrapperStepExecutor())
			.build();
		ExemplarExecutionResult result = ExemplarRunner.create(executor).inDirectory(temporaryFolder.getTestDirectory().toFile()).using(exemplar).run();

		assumeThat(exemplar.getSteps().size(), greaterThan(0));

		for (int i = 0; i < exemplar.getSteps().size(); ++i) {
			Step expected = exemplar.getSteps().get(i);
			StepExecutionResult actual = result.getStepResults().get(i);
			assertThat(actual.getOutcome(), not(equalTo(StepExecutionOutcome.FAILED)));

			if (actual.getOutcome() == StepExecutionOutcome.EXECUTED) {
				assertThat(actual.getExitValue().get(), Matchers.is(0));
				if (expected.getExecutable().equals("./gradlew")) {
					final BuildResult actualBuildResult = BuildResult.from(actual.getOutput().get())
						.withNormalizedTaskOutput(it -> it.getTaskName().equals("xcode"), normalizeXcodePath(getTestDirectory()))
						.withNormalizedTaskOutput(it -> it.getTaskName().equals("outgoingVariants"), normalizeOutputVariantsPath());

					if (expected.getOutput().get().startsWith("...\n") && expected.getOutput().get().endsWith("\n...")) {
						String[] tokens = expected.getOutput().get().split("\n?\\.\\.\\.\n?");
						// the first element is empty because of the first ...\n
						assertThat(tokens.length, greaterThan(1));
						for (int j = 1; j < tokens.length; ++j) {
							assertThat(actualBuildResult.getOutput(), containsString(tokens[j]));
						}
					} else {
						final BuildResult expectedBuildResult = BuildResult.from(expected.getOutput().get());
						assertThat(actualBuildResult, equalTo(expectedBuildResult));
					}
				} else if (expected.getExecutable().equals("tree")) {
					if (actual.getOutput().isPresent()) {
						assertThat(TreeCommandOutput.from(actual.getOutput().get()), equalTo(TreeCommandOutput.from(expected.getOutput().get())));
					}
				} else if (expected.getExecutable().equals("unzip")) {
					if (actual.getOutput().isPresent()) {
						assertThat(UnzipCommandOutput.from(actual.getOutput().get()), equalTo(UnzipCommandOutput.from(expected.getOutput().get())));
					}
				} else if (expected.getExecutable().equals("jar")) {
					assertThat(JarCommandOutput.from(actual.getOutput().get()), equalTo(JarCommandOutput.from(expected.getOutput().get())));
				} else if (expected.getExecutable().equals("xcodebuild")) {
					assertThat(BuildResult.from(actual.getOutput().get()), equalTo(BuildResult.from(expected.getOutput().get())));
				} else {
					if (actual.getOutput().isPresent()) {
						assertThat(actual.getOutput().get(), startsWith(expected.getOutput().get()));
					}
				}
			}
		}
	}

	private static UnaryOperator<String> normalizeXcodePath(File testDirectory) {
		return it -> it.replace(new ConsoleRenderer().asClickableFileUrl(testDirectory), "file://").replace('\\', '/');
	}

	private static UnaryOperator<String> normalizeOutputVariantsPath() {
		return it -> it.replace('\\', '/');
	}

	private final class GradleWrapperStepExecutor implements StepExecutor {
		@Override
		public boolean canHandle(Step step) {
			return step.getExecutable().equals("./gradlew");
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			try {
				GradleRunner executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(context.getCurrentWorkingDirectory()).withRichConsoleEnabled());

				File initScript = new File(context.getCurrentWorkingDirectory(), "init.gradle");
				Files.write(initScript.toPath(), Arrays.asList("",
					"allprojects { p ->",
					"	plugins.withType(NativeComponentModelPlugin) {",
					"		model {",
					"			toolChains {",
					"				" + toolChain.getBuildScriptConfig(),
					"			}",
					"		}",
					"	}",
					"}"));
				executer = toolChain.configureExecuter(executer.usingInitScript(initScript));
				for (String it : context.getCurrentStep().getArguments()) {
					executer = executer.withArgument(it);
				}
				BuildResult result = executer.build();

				return stepExecuted(0, result.getOutput());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private static final class UnzipStepExecutor implements StepExecutor {
		public static File inputFile(StepExecutionContext context) {
			return new File(context.getCurrentWorkingDirectory(), context.getCurrentStep().getArguments().get(0));
		}

		public static File outputDirectory(StepExecutionContext context) {
			return new File(context.getCurrentWorkingDirectory(), context.getCurrentStep().getArguments().get(context.getCurrentStep().getArguments().indexOf("-d") + 1));
		}

		@Override
		public boolean canHandle(Step step) {
			return step.getExecutable().equals("unzip");
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			final Optional<CommandLineTool> tool = CommandLineTool.fromPath(context.getCurrentStep().getExecutable());
			final File inputFile = inputFile(context);
			final File outputDirectory = outputDirectory(context);
			if (tool.isPresent()) {
				String stdout = unzipTo(inputFile, outputDirectory);
				// unzip add extra newline but also have extra tailing spaces
				stdout = stdout.replace(context.getCurrentWorkingDirectory().getAbsolutePath(), "/Users/daniel");
				stdout = separatorsToUnix(stdout);
				return stepExecuted(0, stdout);
			}

			TestFile.of(inputFile).usingNativeTools().unzipTo(outputDirectory);
			return stepExecuted(0);
		}
	}

	private static final class JarStepExecutor implements StepExecutor {
		@Override
		public boolean canHandle(Step step) {
			return step.getExecutable().equals("jar");
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			Optional<CommandLineTool> tool = CommandLineTool.fromPath("jar");
			if (!tool.isPresent()) {
				String javaHome = System.getenv("JAVA_HOME");
				if (javaHome == null && IS_OS_WINDOWS) {
					// Check known location
					javaHome = "C:\\Program Files\\Java\\jdk1.8.0_241";
				}
				assertThat(javaHome, notNullValue());
				tool = Optional.of(CommandLineTool.of(new File(javaHome, OperatingSystem.current().getExecutableName("bin/jar"))));
			}
			final CommandLineToolExecutionResult result = tool.get()
				.withArguments(context.getCurrentStep().getArguments())
				.newInvocation()
				.redirectStandardOutput(toSystemOutput())
				.redirectErrorOutput(toSystemError())
				.workingDirectory(context.getCurrentWorkingDirectory())
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor();

			return stepExecuted(result.getExitValue(), result.getOutput().getAsString());
		}
	}
}
