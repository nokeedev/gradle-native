package dev.nokee.docs.samples

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import org.junit.Rule
import spock.lang.Specification

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
}
