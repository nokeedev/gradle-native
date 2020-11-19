package dev.nokee.docs.samples

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
import dev.nokee.docs.fixtures.*
import dev.nokee.docs.fixtures.html.HtmlTag
import dev.nokee.docs.tags.Baked
import dev.nokee.language.c.internal.UTTypeCSource
import dev.nokee.language.cpp.internal.UTTypeCppSource
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource
import dev.nokee.language.swift.internal.UTTypeSwiftSource
import groovy.io.FileType
import groovy.transform.ToString
import org.apache.commons.lang3.SystemUtils
import org.gradle.internal.logging.ConsoleRenderer
import org.gradle.internal.os.OperatingSystem
import org.junit.Rule
import org.junit.experimental.categories.Category
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.util.concurrent.Callable
import java.util.function.UnaryOperator
import java.util.regex.Pattern

import static dev.nokee.core.exec.CommandLine.of
import static dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect.duplicateToSystemError
import static dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect.duplicateToSystemOutput
import static dev.nokee.utils.DeferredUtils.flatUnpack
import static org.apache.commons.io.FilenameUtils.getExtension
import static org.apache.commons.io.FilenameUtils.separatorsToSystem
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
			settingsEvaluated { settings ->
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
	protected String unzipTo(TestFile zipFile, File workingDirectory) {
		zipFile.assertIsFile()
		workingDirectory.mkdirs()
		def result = CommandLineTool.of('unzip')
			.withArguments(zipFile.getCanonicalPath(), '-d', workingDirectory.getCanonicalPath())
			.newInvocation()
			.redirectStandardOutput(duplicateToSystemOutput())
			.redirectErrorOutput(duplicateToSystemError())
			.workingDirectory(testDirectory)
			.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
			.waitFor(Duration.ofSeconds(30))
			.assertNormalExitValue()

		return result.standardOutput.asString
	}

	@Unroll
	def "ensure root project name is configured for the sample"(dsl) {
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
	def "ensure sample source files matches source layout"(dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		def pluginIdsToExtensions = [
			'dev.nokee.c': UTTypeCSource.INSTANCE.filenameExtensions,
			'dev.nokee.cpp': UTTypeCppSource.INSTANCE.filenameExtensions,
			'dev.nokee.objective-c': UTTypeObjectiveCSource.INSTANCE.filenameExtensions,
			'dev.nokee.objective-cpp': UTTypeObjectiveCppSource.INSTANCE.filenameExtensions,
			'dev.nokee.swift': UTTypeSwiftSource.INSTANCE.filenameExtensions,
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
		def commands = wrap(fixture.commands).findAll { it instanceof GradleWrapperCommand } as List<GradleWrapperCommand>
		assumeThat("Gradle commands are present", commands.size(), greaterThan(0))
		expect:
		commands*.assertNoTimingInformationOnBuildResult()
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
	def "can run './gradlew #taskName' successfully"(taskName, dsl) {
		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		def executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory))
		expect:
		executer.withTasks(taskName).build()

		where:
		taskName << ['help', 'tasks']
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.AVAILABLE
	}

	AvailableToolChains.InstalledToolChain toolChain;
	@Unroll
	def "can execute commands successfully"(dsl) {
		println "Sample under test directory: " + temporaryFolder.testDirectory.absolutePath
		toolChain = AvailableToolChains.getToolChain(toolChainRequirement)
		assumeTrue(toolChain != null && toolChain.meets(ToolChainRequirement.AVAILABLE))

		fixture.getDslSample(dsl).usingNativeTools().unzipTo(temporaryFolder.testDirectory)

		def c = wrapAndGetExecutable(fixture.getCommands())
		assumeThat(c.size(), greaterThan(0));
		expect:
		c.each { it.execute(TestFile.of(temporaryFolder.testDirectory)) }

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	private List<? super Comm> wrapAndGetExecutable(List<Command> commands) {
		return commands.findAll { it.canExecute() }.collect { convert(it) }
	}

	private List<? super Comm> wrap(List<Command> commands) {
		return commands.collect { convert(it) }
	}

	protected Comm convert(Command command) {
		if (command.executable == './gradlew') {
			return new GradleWrapperCommand(command)
		} else if (command.executable == 'ls') {
			return new ListDirectoryCommand(command)
		} else if (command.executable == 'mv') {
			return new MoveFilesCommand(command)
		} else if (command.executable == 'unzip') {
			return new UnzipCommand(command)
		} else if (command.executable == 'tree') {
			return new TreeCommand(command)
		} else if (command.executable == 'jar') {
			return new JarCommand(command)
		} else if (command.executable == 'xcodebuild') {
			return new XcodebuildCommand(command)
		}
		return new GenericCommand(command)
	}

	@ToString
	private static abstract class Comm {
		protected final Command command

		Comm(Command command) {
			this.command = command
		}

		abstract void execute(TestFile testDirectory);
	}

	private class GradleWrapperCommand extends Comm {
		GradleWrapperCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def executer = configureLocalPluginResolution(GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory).withRichConsoleEnabled())

			def initScript = testDirectory.file("init.gradle") << """
				allprojects { p ->
					apply plugin: ${toolChain.pluginClass}

					model {
						toolChains {
							${toolChain.buildScriptConfig}
						}
					}
				}
			"""
			executer = toolChain.configureExecuter(executer.usingInitScript(initScript))
			command.args.each {
				executer = executer.withArgument(it)
			}
			def result = executer.build()
				.withNormalizedTaskOutput({ it.taskName == 'xcode' }, normalizeXcodePath(testDirectory))
				.withNormalizedTaskOutput({ it.taskName == 'outgoingVariants' }, normalizeOutputVariantsPath())


			if (command.expectedOutput.get().startsWith('...\n') && command.expectedOutput.get().endsWith('\n...')) {
				def tokens = command.expectedOutput.get().split("\n?\\.\\.\\.\n?")
				tokens.drop(1) // the first element is empty because of the first ...\n
				assert tokens.size() > 0
				tokens.each {
					assert result.output.contains(it)
				}
			} else {
				def expectedResult = BuildResult.from(command.expectedOutput.get())
				assert result == expectedResult
			}
		}

		private static UnaryOperator<String> normalizeXcodePath(File testDirectory) {
			return { it.replace(new ConsoleRenderer().asClickableFileUrl(testDirectory), 'file://').replace('\\', '/') }
		}

		private static UnaryOperator<String> normalizeOutputVariantsPath() {
			return { it.replace('\\', '/') }
		}

		private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*");
		void assertNoTimingInformationOnBuildResult() {
			command.expectedOutput.ifPresent { output ->
				assert !BUILD_RESULT_PATTERN.matcher(output).find()
			}
		}
	}

	private class XcodebuildCommand extends Comm {
		XcodebuildCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def result = of(command.executable, command.args)
				.newInvocation()
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.workingDirectory(testDirectory)
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor()
				.assertNormalExitValue()
				.output
				.parse { BuildResult.from(it) }

			assert result == BuildResult.from(command.expectedOutput.get())
		}
	}

	private class ListDirectoryCommand extends Comm {

		ListDirectoryCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			if (!SystemUtils.IS_OS_WINDOWS) {
				def result = of(script(command.executable, command.args))
					.newInvocation()
					.redirectStandardOutput(duplicateToSystemOutput())
					.redirectErrorOutput(duplicateToSystemError())
					.workingDirectory(testDirectory)
					.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
					.waitFor()
					.assertNormalExitValue()
				assert result.standardOutput.asString.startsWith(command.expectedOutput.get())
			}
		}
	}

	private class MoveFilesCommand extends Comm {

		MoveFilesCommand(Command command) {
			super(command)
		}

		String getMoveExecutable() {
			if (SystemUtils.IS_OS_WINDOWS) {
				// Trust me? Not too sure if it's equal.
				return 'move'
			}
			return command.executable
		}

		@Override
		void execute(TestFile testDirectory) {
			def result = of(script(moveExecutable, command.args.collect { separatorsToSystem(it) }))
				.newInvocation()
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.workingDirectory(testDirectory)
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor()
				.assertNormalExitValue()

			if (!SystemUtils.IS_OS_WINDOWS) {
				assert result.standardOutput.asString.trim().empty
			}
		}
	}

	private class UnzipCommand extends Comm {
		UnzipCommand(Command command) {
			super(command)
		}

		TestFile inputFile(TestFile testDirectory) {
			return testDirectory.file(command.args[0])
		}

		TestFile outputDirectory(TestFile testDirectory) {
			return testDirectory.file(command.args[command.args.findIndexOf { it == '-d' } + 1])
		}

		@Override
		void execute(TestFile testDirectory) {
			def tool = CommandLineTool.fromPath(command.executable)
			TestFile inputFile = inputFile(testDirectory)
			TestFile outputDirectory = outputDirectory(testDirectory)
			if (tool.isPresent()) {
				String stdout = unzipTo(inputFile, outputDirectory)
				// unzip add extra newline but also have extra tailing spaces
				stdout = stdout.replace(testDirectory.absolutePath, separatorsToSystem('/Users/daniel'))

				assert UnzipCommandHelper.Output.parse(stdout) == UnzipCommandHelper.Output.parse(command.expectedOutput.get())
			} else {
				inputFile.usingNativeTools().unzipTo(outputDirectory)
			}
		}
	}

	private class TreeCommand extends Comm {
		TreeCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			if (!SystemUtils.IS_OS_WINDOWS) {
				def result = of(command.executable, command.args)
					.newInvocation()
					.redirectStandardOutput(duplicateToSystemOutput())
					.redirectErrorOutput(duplicateToSystemError())
					.workingDirectory(testDirectory)
					.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
					.waitFor()
					.assertNormalExitValue()
					.standardOutput
					.parse { TreeCommandHelper.Output.parse(it) }
				assert result == TreeCommandHelper.Output.parse(command.getExpectedOutput().get())
			}
		}
	}

	private class JarCommand extends Comm {
		JarCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def tool = CommandLineTool.fromPath('jar')
			if (!tool.isPresent()) {
				String javaHome = System.getenv('JAVA_HOME')
				if (javaHome == null && SystemUtils.IS_OS_WINDOWS) {
					// Check known location
					javaHome = "C:\\Program Files\\Java\\jdk1.8.0_241"
				}
				assert javaHome != null
				tool = Optional.of(CommandLineTool.of(new File(javaHome, OperatingSystem.current().getExecutableName("bin/jar"))))
			}
			def result = tool.get()
				.withArguments(command.args)
				.newInvocation()
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.workingDirectory(testDirectory)
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor()
				.assertNormalExitValue()
				.standardOutput
				.parse {JarCommandHelper.Output.parse(it) }

			assert result == JarCommandHelper.Output.parse(command.getExpectedOutput().get())
		}
	}

	static Callable<List<Object>> getScriptCommandLine() {
		return {
			if (SystemUtils.IS_OS_WINDOWS) {
				return ['cmd', '/c']
			}
			return ['/bin/bash', '-c']
		}
	}

	static List<Object> script(Object... objects) {
		return [scriptCommandLine, flatUnpack(Arrays.asList(objects)).join(' ')]
	}

	private class GenericCommand extends Comm {
		GenericCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def result = of(script(separatorsToSystem(command.executable), command.args))
				.newInvocation()
				.workingDirectory(testDirectory)
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor()
				.assertNormalExitValue()

			assert result.standardOutput.withNormalizedEndOfLine().asString.startsWith(command.getExpectedOutput().get())
		}
	}
}
