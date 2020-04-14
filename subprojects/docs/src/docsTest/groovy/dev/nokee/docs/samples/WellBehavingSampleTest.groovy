package dev.nokee.docs.samples

import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleExecuterFactory
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.LogContent
import dev.gradleplugins.test.fixtures.gradle.executer.OutputScrapingExecutionResult
import dev.gradleplugins.test.fixtures.logging.ConsoleOutput
import dev.nokee.docs.fixtures.Command
import dev.nokee.docs.fixtures.SampleContentFixture
import dev.nokee.docs.fixtures.TreeCommandHelper
import dev.nokee.docs.fixtures.UnzipCommandHelper
import dev.nokee.docs.fixtures.html.HtmlTag
import dev.nokee.docs.tags.Baked
import groovy.transform.ToString
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import org.gradle.internal.os.OperatingSystem
import org.junit.Rule
import org.junit.experimental.categories.Category
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

	protected GradleExecuter configureLocalPluginResolution(GradleExecuter executer) {
		def initScriptFile = temporaryFolder.file('repo.init.gradle')
		initScriptFile << """
			settingsEvaluated { settings ->
				settings.pluginManagement {
					repositories {
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
		def fixture = new SampleContentFixture(sampleName)
		fixture.getDslSample(dsl).unzipTo(temporaryFolder.testDirectory)

		expect:
		// TODO: Improve assertion to ensure it's rootProject.name = <sampleName> and not just a random <sampleName> in the settings script
		testDirectory.file(dsl.settingsFileName).assertIsFile().text.contains(sampleName)

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	def "ensure sample has a category"() {
		def fixture = new SampleContentFixture(sampleName)

		expect:
		fixture.category != null
	}

	def "ensure sample has a valid summary"() {
		def fixture = new SampleContentFixture(sampleName)

		expect:
		fixture.summary != null
		fixture.summary.endsWith('.')
	}

	/**
	 * Timing values are heavily dependent on the system where the command was executed.
	 * It's better to remove the information to avoid creating false or misleading expectation on the performance.
	 */
	def "ensure gradle commands does not have any timing values in build result"() {
		def fixture = new SampleContentFixture(sampleName)

		expect:
		// TODO: Reports all the error at once instead of failing on the first one
		def commands = wrap(fixture.commands).findAll { it instanceof GradleWrapperCommand } as List<GradleWrapperCommand>
		commands*.assertNoTimingInformationOnBuildResult()
	}

	@Category(Baked)
	def "has the twitter player meta data"() {
		expect:
		def fixture = new SampleContentFixture(sampleName)
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
		def fixture = new SampleContentFixture(sampleName)
		fixture.getDslSample(dsl).unzipTo(temporaryFolder.testDirectory)

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
		toolChain = AvailableToolChains.getDefaultToolChain()
		// TODO: Allow better way to select a toolchain
		assumeTrue(toolChain.meets(toolChainRequirement));

		def fixture = new SampleContentFixture(sampleName)
		fixture.getDslSample(dsl).unzipTo(temporaryFolder.testDirectory)

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

			assert OutputScrapingExecutionResult.normalize(LogContent.of(result.getPlainTextOutput())).replace(' in 0s', '').startsWith(expectedResult.getOutput())
		}

		private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*");
		void assertNoTimingInformationOnBuildResult() {
			command.expectedOutput.ifPresent { output ->
				assert !BUILD_RESULT_PATTERN.matcher(output).find()
			}
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
				assert process.in.text.startsWith(command.expectedOutput.get())
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
			def tool = new ToolFromPath(command.args[0])
			TestFile inputFile = testDirectory.file(command.args[0])
			TestFile outputDirectory = testDirectory.file(command.args[command.args.findIndexOf { it == '-d' } + 1])
			if (tool.available) {
				String stdout = unzipTo(inputFile, outputDirectory)
				// unzip add extra newline but also have extra tailing spaces
				stdout = stdout.replace(testDirectory.absolutePath, '/Users/daniel')

				assert UnzipCommandHelper.Output.parse(stdout) == UnzipCommandHelper.Output.parse(command.expectedOutput.get())
			} else {
				inputFile.unzipTo(outputDirectory)
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
				def process = (['/bin/bash', '-c'] + ([command.executable] + command.args).join(' ')).execute(null, testDirectory)
				assert process.waitFor() == 0
				def stdout = process.in.text
				println stdout // TODO: Port this capability to other commands

				assert TreeCommandHelper.Output.parse(stdout) == TreeCommandHelper.Output.parse(command.getExpectedOutput().get())
			}
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
			def process = (scriptCommandLine + ([executable] + command.args).join(' ')).execute(null, testDirectory)
			assert process.waitFor() == 0
			def stdout = process.in.text
			println stdout // TODO: Port this capability to other commands

			// TODO: Not technically right to use LogContent as it has some opinion on Gradle execution
			assert LogContent.of(stdout).withNormalizedEol().startsWith(command.getExpectedOutput().get())
		}
	}
}
