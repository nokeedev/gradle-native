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
package dev.nokee.docs.samples

import dev.gradleplugins.exemplarkit.*
import dev.gradleplugins.exemplarkit.output.JarCommandOutput
import dev.gradleplugins.exemplarkit.output.TreeCommandOutput
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutput
import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.runnerkit.BuildResult
import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner
import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.nokee.core.exec.CommandLineTool
import dev.nokee.core.exec.LoggingEngine
import dev.nokee.core.exec.ProcessBuilderEngine
import dev.nokee.docs.fixtures.OnlyIfCondition
import dev.nokee.docs.fixtures.SampleContentFixture
import dev.nokee.docs.fixtures.html.HtmlTag
import dev.nokee.docs.tags.Baked
import groovy.io.FileType
import org.gradle.internal.logging.ConsoleRenderer
import org.gradle.internal.os.OperatingSystem
import org.junit.Rule
import org.junit.experimental.categories.Category
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.util.function.UnaryOperator
import java.util.regex.Pattern

import static dev.gradleplugins.exemplarkit.StepExecutionResult.stepExecuted
import static dev.gradleplugins.exemplarkit.StepExecutors.replaceIfAbsent
import static dev.gradleplugins.exemplarkit.StepExecutors.skipIf
import static dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect.duplicateToSystemError
import static dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect.duplicateToSystemOutput
import static org.apache.commons.io.FilenameUtils.getExtension
import static org.apache.commons.io.FilenameUtils.separatorsToUnix
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
import static org.hamcrest.Matchers.greaterThan
import static org.junit.Assume.assumeThat
import static org.junit.Assume.assumeTrue

@CleanupTestDirectory
abstract class WellBehavingSampleTest extends Specification {
	@Rule
	final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

	@Shared def fixture = new SampleContentFixture(sampleName)

	protected GradleRunner configureLocalPluginResolution(GradleRunner runner) {
		def initScriptFile = temporaryFolder.file('repo.init.gradle')
		initScriptFile << """
			beforeSettings { settings ->
				settings.pluginManagement {
					repositories {
						gradlePluginPortal()
						maven {
							name = 'docs'
							url = '${new File(System.getProperty("dev.nokee.docsRepository")).toURI().toString()}'
						}
					}
				}
			}
		"""
		return runner.usingInitScript(initScriptFile)
	}

	protected abstract String getSampleName();

	protected TestFile getTestDirectory() {
		return TestFile.of(temporaryFolder.testDirectory)
	}

	// TODO: Migrate to TestFile
	protected static String unzipTo(File zipFile, File workingDirectory) {
		assert zipFile.isFile()
		workingDirectory.mkdirs()
		def result = CommandLineTool.of('unzip')
			.withArguments(zipFile.getCanonicalPath(), '-d', workingDirectory.getCanonicalPath())
			.newInvocation()
			.redirectStandardOutput(duplicateToSystemOutput())
			.redirectErrorOutput(duplicateToSystemError())
			.workingDirectory(workingDirectory)
			.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
			.waitFor(Duration.ofSeconds(30))
			.assertNormalExitValue()

		return result.standardOutput.asString
	}

	@Unroll
	def "ensure root project name is configured for the sample [#dsl]"(dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		expect:
		// TODO: Improve assertion to ensure it's rootProject.name = <sampleName> and not just a random <sampleName> in the settings script
		testDirectory.file(dsl.settingsFileName).assertIsFile().text.contains(sampleName)

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	def "ensure sample has a category"() {
		expect:
		fixture.category != null
	}

	def "ensure sample has a valid summary"() {
		expect:
		fixture.summary != null
		fixture.summary.endsWith('.')
	}

	List<String> getExpectedAdditionalExtensions() {
		return []
	}

	@Unroll
	def "ensure sample source files matches source layout [#dsl]"(dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		def pluginIdsToExtensions = [
			'dev.nokee.c': ['c'],
			'dev.nokee.cpp': ['cp', 'cpp', 'c++', 'cc', 'cxx'],
			'dev.nokee.objective-c': ['m'],
			'dev.nokee.objective-cpp': ['mm'],
			'dev.nokee.swift': ['swift'],
			'dev.gradleplugins.java': ['java'],
			'dev.gradleplugins.groovy': ['groovy'],
			'java': ['java'],
			'groovy': ['groovy'],
			'org.jetbrains.kotlin.jvm': ['kt'],
			'cpp': ['cpp']
		]
		def languageExtensions = pluginIdsToExtensions.values().flatten()
		def allBuildFiles = []
		def allFilesByExtensions = [:].withDefault { [] }
		testDirectory.eachFileRecurse(FileType.FILES) {
			if (GradleScriptDsl.values()*.buildFileName.contains(it.name)) {
				allBuildFiles.add(it)
			} else if (languageExtensions.contains(getExtension(it.name))) {
				allFilesByExtensions.get(getExtension(it.name)).add(it)
			}
		}

		def allAppliedPlugins = allBuildFiles.collect {
			def m = (it.text =~ /id[( ]["'](.+?)['"]\)?/)
			def result = []
			for (i in 0..<m.count) {
				result.add(m[i][1])
			}
			return result
		}.flatten()

		println "Found files: ${allFilesByExtensions}"
		println "Found applied plugins: ${allAppliedPlugins}"

		pluginIdsToExtensions.retainAll { k, v -> allAppliedPlugins.any { it.startsWith(k) } }
		def effectiveLanguageExtensions = pluginIdsToExtensions.values().flatten() + expectedAdditionalExtensions
		println "Effective language extensions: ${effectiveLanguageExtensions}"

		allFilesByExtensions.removeAll { k, v -> effectiveLanguageExtensions.contains(k) }
		println "Left over files: ${allFilesByExtensions}"

		expect:
		allFilesByExtensions.isEmpty()

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	/**
	 * Timing values are heavily dependent on the system where the command was executed.
	 * It's better to remove the information to avoid creating false or misleading expectation on the performance.
	 */
	def "ensure gradle commands does not have any timing values in build result"() {
		// TODO: Reports all the error at once instead of failing on the first one
		def gradleSteps = fixture.getDslExemplar(GradleScriptDsl.GROOVY_DSL).steps.findAll { it.executable.endsWith('gradlew') }
		assumeThat("Gradle commands are present", gradleSteps.size(), greaterThan(0))
		expect:
		gradleSteps.each { assertNoTimingInformationOnBuildResult(it) }
	}

	private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*");
	private static void assertNoTimingInformationOnBuildResult(Step step) {
		step.output.ifPresent { output ->
			assert !BUILD_RESULT_PATTERN.matcher(output).find()
		}
	}

	@Category(Baked)
	def "has the twitter player meta data"() {
		expect:
		def it = fixture.bakedFile
		def twitterImages = it.findAll(HtmlTag.META).findAll { it.twitterImage }
		assert twitterImages.size() == 1, "${it.uri} does not have the right meta twitter image tag count"
		assert twitterImages.first().content == "${it.canonicalPath}all-commands.png"

		and:
		def twitterCards = it.findAll(HtmlTag.META).findAll { it.twitterCard }
		assert twitterCards.size() == 1, "${it.uri} does not have the right meta twitter card tag count"
		assert twitterCards.first().content == "player"

		and:
		def twitterPlayers = it.findAll(HtmlTag.META).findAll { it.twitterPlayer }
		assert twitterPlayers.size() == 1, "${it.uri} does not have the right meta twitter player tag count"
		assert twitterPlayers.first().content == "${it.canonicalPath}all-commands.embed.html"
	}

	@Unroll
	def "can run './gradlew #taskName' successfully [#dsl]"(taskName, dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		def executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory))
		expect:
		executer.withTasks(taskName).build()

		where:
		[taskName, dsl] << [['help', 'tasks'], [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]].combinations()
	}

	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.AVAILABLE
	}

	AvailableToolChains.InstalledToolChain toolChain;
	@Unroll
	def "can execute commands successfully [#dsl]"(dsl) {
		println "Sample under test directory: " + temporaryFolder.testDirectory.absolutePath
		toolChain = AvailableToolChains.getToolChain(toolChainRequirement)
		assumeTrue(toolChain != null && toolChain.meets(ToolChainRequirement.AVAILABLE))

		def exemplar = fixture.getDslExemplar(dsl)
		ExemplarExecutor executor = ExemplarExecutor.builder()
			.registerCommandLineToolExecutor(skipIf { !OnlyIfCondition.of(Objects.toString(it.attributes.get('only-if'), null)).canExecute() })
			.registerCommandLineToolExecutor(new JarStepExecutor())
			.registerCommandLineToolExecutor(skipIf {it.executable == 'tree' && IS_OS_WINDOWS })
			.registerCommandLineToolExecutor(new UnzipStepExecutor())
			.registerCommandLineToolExecutor(skipIf { it.executable == 'ls' && IS_OS_WINDOWS })
			.registerCommandLineToolExecutor(replaceIfAbsent('mv', 'move'))
			.registerCommandLineToolExecutor(new GradleWrapperStepExecutor())
			.build()
		def result = ExemplarRunner.create(executor).inDirectory(temporaryFolder.testDirectory).using(exemplar).run()

		assumeThat(exemplar.steps.size(), greaterThan(0))
		expect:
		[exemplar.steps, result.stepResults].transpose().each { Step expected, StepExecutionResult actual ->
			assert actual.outcome != StepExecutionOutcome.FAILED

			if (actual.outcome == StepExecutionOutcome.EXECUTED) {
				assert actual.exitValue.get() == 0
				if (expected.executable == './gradlew') {
					def actualBuildResult = BuildResult.from(actual.output.get())
						.withNormalizedTaskOutput({ it.taskName == 'xcode' }, normalizeXcodePath(testDirectory))
						.withNormalizedTaskOutput({ it.taskName == 'outgoingVariants' }, normalizeOutputVariantsPath())

					if (expected.output.get().startsWith('...\n') && expected.output.get().endsWith('\n...')) {
						def tokens = expected.output.get().split("\n?\\.\\.\\.\n?")
						tokens.drop(1)
						// the first element is empty because of the first ...\n
						assert tokens.size() > 0
						tokens.each {
							assert actualBuildResult.output.contains(it)
						}
					} else {
						def expectedBuildResult = BuildResult.from(expected.output.get())
						assert actualBuildResult == expectedBuildResult
					}
				} else if (expected.executable == 'tree') {
					if (actual.output.present) {
						assert TreeCommandOutput.from(actual.output.get()) == TreeCommandOutput.from(expected.output.get())
					}
				} else if (expected.executable == 'unzip') {
					if (actual.output.present) {
						assert UnzipCommandOutput.from(actual.output.get()) == UnzipCommandOutput.from(expected.output.get())
					}
				} else if (expected.executable == 'jar') {
					assert JarCommandOutput.from(actual.output.get()) == JarCommandOutput.from(expected.output.get())
				} else if (expected.executable == 'xcodebuild') {
					assert BuildResult.from(actual.output.get()) == BuildResult.from(expected.output.get())
				} else {
					if (actual.output.present) {
						assert actual.output.get().startsWith(expected.output.get())
					}
				}
			}
		}

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	private static UnaryOperator<String> normalizeXcodePath(File testDirectory) {
		return { it.replace(new ConsoleRenderer().asClickableFileUrl(testDirectory), 'file://').replace('\\', '/') }
	}

	private static UnaryOperator<String> normalizeOutputVariantsPath() {
		return { it.replace('\\', '/') }
	}

	private final class GradleWrapperStepExecutor implements StepExecutor {
		@Override
		boolean canHandle(Step step) {
			return step.executable == './gradlew'
		}

		@Override
		StepExecutionResult run(StepExecutionContext context) {
			def executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(context.currentWorkingDirectory).withRichConsoleEnabled())

			def initScript = new File(context.currentWorkingDirectory, "init.gradle") << """
				allprojects { p ->
					plugins.withType(NativeComponentModelPlugin) {
						model {
							toolChains {
								${toolChain.buildScriptConfig}
							}
						}
					}
				}
			"""
			executer = toolChain.configureExecuter(executer.usingInitScript(initScript))
			context.currentStep.arguments.each {
				executer = executer.withArgument(it)
			}
			def result = executer.build()

			return stepExecuted(0, result.output)
		}
	}

	private static final class UnzipStepExecutor implements StepExecutor {
		static File inputFile(StepExecutionContext context) {
			return new File(context.currentWorkingDirectory, context.currentStep.arguments[0])
		}

		static File outputDirectory(StepExecutionContext context) {
			return new File(context.currentWorkingDirectory, context.currentStep.arguments[context.currentStep.arguments.findIndexOf { it == '-d' } + 1])
		}

		@Override
		boolean canHandle(Step step) {
			return step.executable == 'unzip'
		}

		@Override
		StepExecutionResult run(StepExecutionContext context) {
			def tool = CommandLineTool.fromPath(context.currentStep.executable)
			def inputFile = inputFile(context)
			def outputDirectory = outputDirectory(context)
			if (tool.isPresent()) {
				String stdout = unzipTo(inputFile, outputDirectory)
				// unzip add extra newline but also have extra tailing spaces
				stdout = stdout.replace(context.currentWorkingDirectory.absolutePath, '/Users/daniel')
				stdout = separatorsToUnix(stdout)
				return stepExecuted(0, stdout)
			}

			TestFile.of(inputFile).usingNativeTools().unzipTo(outputDirectory)
			return stepExecuted(0)
		}
	}

	private static final class JarStepExecutor implements StepExecutor {
		@Override
		boolean canHandle(Step step) {
			return step.executable == 'jar'
		}

		@Override
		StepExecutionResult run(StepExecutionContext context) {
			def tool = CommandLineTool.fromPath('jar')
			if (!tool.isPresent()) {
				String javaHome = System.getenv('JAVA_HOME')
				if (javaHome == null && IS_OS_WINDOWS) {
					// Check known location
					javaHome = "C:\\Program Files\\Java\\jdk1.8.0_241"
				}
				assert javaHome != null
				tool = Optional.of(CommandLineTool.of(new File(javaHome, OperatingSystem.current().getExecutableName("bin/jar"))))
			}
			def result = tool.get()
				.withArguments(context.currentStep.arguments)
				.newInvocation()
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.workingDirectory(context.currentWorkingDirectory)
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor()

			return stepExecuted(result.exitValue, result.output.asString)
		}
	}
}
