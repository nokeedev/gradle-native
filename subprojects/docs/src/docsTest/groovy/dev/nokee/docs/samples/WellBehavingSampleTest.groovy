package dev.nokee.docs.samples

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
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@CleanupTestDirectory
abstract class WellBehavingSampleTest extends Specification {
	@Rule
	final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

	protected GradleExecuter configureLocalPluginResolution(GradleExecuter executer) {
		def initScriptFile = temporaryFolder.file('repo.init.gradle')
		initScriptFile << """
			settingsEvaluated { settings ->
				settings.pluginManagement {
					repositories {
						maven {
							name = 'docs'
							url = '${System.getProperty('dev.nokee.docsRepository')}'
						}
					}
				}
			}
		"""
		return executer.usingInitScript(initScriptFile)
	}

	// TODO TEST: Ensure settings.gradle[.kts] contains sample name as rootProject.name

	protected abstract String getSampleName();

	// TODO: Migrate to TestFile
	protected void unzipTo(TestFile zipFile, File workingDirectory) {
		zipFile.assertIsFile()
		workingDirectory.mkdirs()
		assertSuccessfulExecution(['unzip', zipFile.getCanonicalPath(), '-d', workingDirectory.getCanonicalPath()])
	}

	private void assertSuccessfulExecution(List<String> commandLine, File workingDirectory = null) {
		def process = commandLine.execute(null, workingDirectory)
		def stdoutThread = Thread.start { process.in.eachByte { print(new String(it)) } }
		def stderrThread = Thread.start { process.err.eachByte { print(new String(it)) } }
		assert process.waitFor(30, TimeUnit.SECONDS)
		assert process.exitValue() == 0
		stdoutThread.join(5000)
		stderrThread.join(5000)
	}

	@Unroll
	def "can run './gradlew #taskName' successfully"(taskName, dsl) {
		def fixture = new SampleContentFixture(sampleName)
		unzipTo(fixture.getDslSample(dsl), temporaryFolder.testDirectory)

		GradleExecuter executer = configureLocalPluginResolution(new GradleExecuterFactory().wrapper(TestFile.of(temporaryFolder.testDirectory)))
		expect:
		executer.withTasks(taskName).run()

		where:
		taskName << ['help', 'tasks']
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	def "can execute commands successfully"(dsl) {
		def fixture = new SampleContentFixture(sampleName)
		unzipTo(fixture.getDslSample(dsl), temporaryFolder.testDirectory)

		expect:
		def c = wrap(fixture.getCommands())
		c.each { it.execute(temporaryFolder.testDirectory) }

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	private List<? super Comm> wrap(List<Command> commands) {
		commands.collect { command ->
			if (command.executable == './gradlew') {
				return new GradleWrapperCommand(command)
			}
			return new GenericCommand(command)
		}
	}

	private static abstract class Comm {
		protected final Command command

		Comm(Command command) {
			this.command = command
		}

		public abstract void execute(File testDirectory);
	}

	private class GradleWrapperCommand extends Comm {
		GradleWrapperCommand(Command command) {
			super(command)
		}

		@Override
		void execute(File testDirectory) {
			GradleExecuter executer = configureLocalPluginResolution(new GradleExecuterFactory().wrapper(TestFile.of(testDirectory)).withConsole(ConsoleOutput.Rich))

			def result = executer.withArguments(command.args).run()
			def expectedResult = OutputScrapingExecutionResult.from(command.expectedOutput.get(), '')

			assert OutputScrapingExecutionResult.normalize(LogContent.of(result.getPlainTextOutput())).replace(' in 0s', '').startsWith(expectedResult.getOutput())
		}
	}

	private static class GenericCommand extends Comm {
		GenericCommand(Command command) {
			super(command)
		}

		@Override
		void execute(File testDirectory) {
			throw new UnsupportedOperationException("bob")
		}
	}
}
