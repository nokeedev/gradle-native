package dev.nokee.docs.samples

import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleExecuterFactory
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.LogContent
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult
import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput
import dev.nokee.docs.fixtures.*
import dev.nokee.docs.fixtures.html.HtmlTag
import dev.nokee.docs.tags.Baked
import groovy.transform.ToString
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import org.gradle.internal.logging.ConsoleRenderer
import org.gradle.internal.os.OperatingSystem
import org.junit.Rule
import org.junit.experimental.categories.Category
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import static org.hamcrest.Matchers.greaterThan
import static org.junit.Assume.assumeThat
import static org.junit.Assume.assumeTrue

@CleanupTestDirectory
abstract class WellBehavingSampleTest extends Specification {
	@Rule
	final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

	@Shared def fixture = new SampleContentFixture(sampleName)

	protected GradleExecuter configureLocalPluginResolution(GradleExecuter executer) {
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
		return executer.usingInitScript(initScriptFile)
	}

	protected abstract String getSampleName();

	protected TestFile getTestDirectory() {
		return TestFile.of(temporaryFolder.testDirectory)
	}

	// TODO: Migrate to TestFile
	protected String unzipTo(TestFile zipFile, File workingDirectory) {
		zipFile.assertIsFile()
		workingDirectory.mkdirs()
		return assertSuccessfulExecution(['unzip', zipFile.getCanonicalPath(), '-d', workingDirectory.getCanonicalPath()])
	}

	private String assertSuccessfulExecution(List<String> commandLine, File workingDirectory = null) {
		def process = commandLine.execute(null, workingDirectory)
		def outStream = new ByteArrayOutputStream()
		def stdoutThread = Thread.start { process.in.eachByte { print(new String(it)); outStream.write(it) } }
		def stderrThread = Thread.start { process.err.eachByte { print(new String(it)) } }
		assert process.waitFor(30, TimeUnit.SECONDS)
		assert process.exitValue() == 0
		stdoutThread.join(5000)
		stderrThread.join(5000)
		return outStream.toString()
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

		GradleExecuter executer = configureLocalPluginResolution(new GradleExecuterFactory().wrapper(TestFile.of(temporaryFolder.testDirectory)))
		expect:
		executer.withTasks(taskName).run()

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
			GradleExecuter executer = configureLocalPluginResolution(new GradleExecuterFactory().wrapper(TestFile.of(testDirectory)).withConsole(ConsoleOutput.Rich))

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
			def result = executer.run()
			def expectedResult = OutputScrapingExecutionResult.from(command.expectedOutput.get(), '')

			def asserter = new StartsWithOutputAsserter()
			if (command.expectedOutput.get().startsWith('...\n') && command.expectedOutput.get().endsWith('\n...')) {
				asserter = new ContainsOutputAsserter()
			}

			def stdout = result.getPlainTextOutput()
			if (SystemUtils.IS_OS_WINDOWS) {
				stdout = result.getPlainTextOutput().split('\n').drop(2).join('\n')
			}

			// TODO: The file path normalizer should be taken cared as either: 1) a per Gradle task output normalizer or 2) a general path like normalizer across the entire output
			//    The second option will need to be verbose (at least log when it's replacing paths)
			//    Here we did a poor-man per task normalizer but it's simply because we don't detect what "look like a path", we dumbly convert \\ to /
			if (command.args.contains('outgoingVariants')) {
				asserter.assertOutput(OutputScrapingExecutionResult.normalize(LogContent.of(stdout)).replace(' in 0s', '').replace('\\', '/'), expectedResult.getOutput())
			} else if (command.args.contains('xcode')) {
				asserter.assertOutput(OutputScrapingExecutionResult.normalize(LogContent.of(stdout)).replace(' in 0s', '').replace(new ConsoleRenderer().asClickableFileUrl(testDirectory), 'file://').replace('\\', '/'), expectedResult.getOutput())
			} else {
				asserter.assertOutput(OutputScrapingExecutionResult.normalize(LogContent.of(stdout)).replace(' in 0s', ''), expectedResult.getOutput())
			}
		}

		private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*");
		void assertNoTimingInformationOnBuildResult() {
			command.expectedOutput.ifPresent { output ->
				assert !BUILD_RESULT_PATTERN.matcher(output).find()
			}
		}
	}

	interface OutputAsserter {
		void assertOutput(String stdout, String expected)
	}

	class StartsWithOutputAsserter implements OutputAsserter {
		@Override
		void assertOutput(String stdout, String expected) {
			assert stdout.startsWith(expected)
		}
	}

	class ContainsOutputAsserter implements OutputAsserter {
		@Override
		void assertOutput(String stdout, String expected) {
			def tokens = expected.split("\n?\\.\\.\\.\n?")
			tokens.drop(1) // the first element is empty because of the first ...\n
			assert tokens.size() > 0
			tokens.each {
				assert stdout.contains(it)
			}
		}
	}

	private class XcodebuildCommand extends Comm {
		XcodebuildCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def process = ([command.executable] + command.args).execute(null, testDirectory)
			int exitCode = process.waitFor()
			def stdout = process.in.text
			def stderr = process.err.text
			println stdout
			if (exitCode != 0) {
				println stderr
			}
			assert exitCode == 0

			def expectedResult = OutputScrapingExecutionResult.from(command.expectedOutput.get().split('\n').dropWhile { !it.startsWith('> Task') }.join('\n'), '')
			def result = OutputScrapingExecutionResult.from(stdout.split('\n').dropWhile { !it.startsWith('> Task') }.join('\n'), '')

			assert OutputScrapingExecutionResult.normalize(LogContent.of(result.getPlainTextOutput())).replace(' in 0s', '').startsWith(expectedResult.getOutput())
		}
	}

	private class ListDirectoryCommand extends Comm {

		ListDirectoryCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			if (!SystemUtils.IS_OS_WINDOWS) {
				def scriptCommandLine = ['/bin/bash', '-c']
				def executable = command.executable
				def process = (scriptCommandLine + [([executable] + command.args).join(' ')]).execute(null, testDirectory)
				assert process.waitFor() == 0
				def stdout = process.in.text
				println stdout
				assert stdout.startsWith(command.expectedOutput.get())
			}
		}
	}

	private class MoveFilesCommand extends Comm {

		MoveFilesCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def scriptCommandLine = ['/bin/bash', '-c']
			if (SystemUtils.IS_OS_WINDOWS) {
				scriptCommandLine = ['cmd', '/c']
			}

			def executable = command.executable
			if (SystemUtils.IS_OS_WINDOWS) {
				// Trust me? Not too sure if it's equal.
				executable = 'move'
			}
			def process = (scriptCommandLine + ([executable] + command.args.collect { FilenameUtils.separatorsToSystem(it) }).join(' ')).execute(null, testDirectory)
			assert process.waitFor() == 0
			if (!SystemUtils.IS_OS_WINDOWS) {
				assert process.in.text.trim().empty
			}
		}
	}

	private class UnzipCommand extends Comm {
		UnzipCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def tool = new ToolFromPath(command.executable)
			TestFile inputFile = testDirectory.file(command.args[0])
			TestFile outputDirectory = testDirectory.file(command.args[command.args.findIndexOf { it == '-d' } + 1])
			if (tool.available) {
				String stdout = unzipTo(inputFile, outputDirectory)
				// unzip add extra newline but also have extra tailing spaces
				stdout = stdout.replace(testDirectory.absolutePath, '/Users/daniel')

				assert UnzipCommandHelper.Output.parse(stdout) == UnzipCommandHelper.Output.parse(command.expectedOutput.get())
			} else {
				inputFile.usingNativeTools().unzipTo(outputDirectory)
			}
		}
	}

	// TODO: Migrate to core code
	private static class ToolFromPath {
		private final String executable

		ToolFromPath(String executable) {
			// TODO: executable should not container File.separator
			this.executable = executable
		}

		boolean isAvailable() {
			return OperatingSystem.current().findInPath(executable) != null
		}

		File get() {
			// TODO: Throw exception if not available
			return OperatingSystem.current().findInPath(executable)
		}
	}

	private class TreeCommand extends Comm {
		TreeCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			if (!SystemUtils.IS_OS_WINDOWS) {
				def process = ([command.executable] + command.args).execute(null, testDirectory)
				assert process.waitFor() == 0
				def stdout = process.in.text
				println stdout // TODO: Port this capability to other commands

				assert TreeCommandHelper.Output.parse(stdout) == TreeCommandHelper.Output.parse(command.getExpectedOutput().get())
			}
		}
	}

	private class JarCommand extends Comm {
		JarCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def tool = new ToolFromPath('jar')
			if (!tool.isAvailable()) {
				String javaHome = System.getenv('JAVA_HOME')
				if (javaHome == null && SystemUtils.IS_OS_WINDOWS) {
					// Check known location
					javaHome = "C:\\Program Files\\Java\\jdk1.8.0_241"
				}
				assert javaHome != null
				tool = new Tool(new File(javaHome, OperatingSystem.current().getExecutableName("bin/jar")))
			}
			def process = ([tool.get()] + command.args).execute(null, testDirectory)
			assert process.waitFor() == 0
			def stdout = process.in.text
			println stdout // TODO: Port this capability to other commands

			assert JarCommandHelper.Output.parse(stdout).equals(JarCommandHelper.Output.parse(command.getExpectedOutput().get()))
		}
	}

	private static class Tool {
		private final File executable

		Tool(File executable) {
			// TODO: executable should not container File.separator
			this.executable = executable
		}

		boolean isAvailable() {
			return executable.exists()
		}

		File get() {
			// TODO: Throw exception if not found
			return executable
		}
	}

	private class GenericCommand extends Comm {
		GenericCommand(Command command) {
			super(command)
		}

		@Override
		void execute(TestFile testDirectory) {
			def scriptCommandLine = ['/bin/bash', '-c']
			if (SystemUtils.IS_OS_WINDOWS) {
				scriptCommandLine = ['cmd', '/c']
			}
			def executable = FilenameUtils.separatorsToSystem(command.executable)
			println "Executing ${([executable] + command.args).join(' ')}"
			def process = (scriptCommandLine + ([executable] + command.args).join(' ')).execute(null, testDirectory)
			int exitCode = process.waitFor()
			def stdout = process.in.text
			def stderr = process.err.text
			println stdout // TODO: Port this capability to other commands
			if (exitCode != 0) {
				println stderr
			}
			assert exitCode == 0
			// TODO: Not technically right to use LogContent as it has some opinion on Gradle execution
			assert LogContent.of(stdout).withNormalizedEol().startsWith(command.getExpectedOutput().get())
		}
	}
}
