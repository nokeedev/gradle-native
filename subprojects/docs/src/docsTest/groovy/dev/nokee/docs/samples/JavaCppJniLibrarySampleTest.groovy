package dev.nokee.docs.samples

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleExecuterFactory
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@CleanupTestDirectory
class JavaCppJniLibrarySampleTest extends Specification {
	@Rule
	final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

	String getSampleName() {
		return 'java-cpp-jni-library'
	}

	@Unroll
	def "can run './gradlew #taskName' successfully"(taskName) {
		def fixture = new SampleContentFixture(sampleName)
		unzipTo(fixture.groovyDslSample, temporaryFolder.testDirectory)

		GradleExecuter executer = new GradleExecuterFactory().wrapper(TestFile.of(temporaryFolder.testDirectory))
		expect:
		executer.withTasks(taskName).run()

		where:
		taskName << ['help', 'tasks']
	}

	// TODO: Migrate to TestFile
	void unzipTo(TestFile zipFile, File workingDirectory) {
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
