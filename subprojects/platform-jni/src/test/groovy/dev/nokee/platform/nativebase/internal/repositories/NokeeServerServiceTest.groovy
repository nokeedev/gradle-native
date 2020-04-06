package dev.nokee.platform.nativebase.internal.repositories

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.nokee.platform.nativebase.internal.plugins.FakeMavenRepositoryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject
import spock.util.environment.OperatingSystem

import java.util.logging.Handler
import java.util.logging.LogManager
import java.util.logging.LogRecord

@Subject(NokeeServerService)
@CleanupTestDirectory
@Requires({ OperatingSystem.current.macOs })
class NokeeServerServiceTest extends Specification {
	@Rule
	private final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

	def "choose a free random port to listen on"() {
		given:
		def takeOverTheHardCodedPort = new ServerSocket(9666) // used to use a static port
		def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.testDirectory).build()

		when:
		project.apply plugin: FakeMavenRepositoryPlugin

		then:
		noExceptionThrown()

		cleanup:
		takeOverTheHardCodedPort.close()
		project.gradle.sharedServices.registrations.nokeeServer.service.get().close()
	}

	def "only start one server"() {
		given:
		LogHandler log = new LogHandler()
		LogManager.logManager.getLogger("").addHandler(log)
		def rootProject = ProjectBuilder.builder().withProjectDir(temporaryFolder.testDirectory).withName('root').build()
		def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.createDirectory('subproject')).withParent(rootProject).build()

		when:
		rootProject.apply plugin: FakeMavenRepositoryPlugin
		project.apply plugin: FakeMavenRepositoryPlugin

		then:
		noExceptionThrown()

		and:
		log.output.count('Nokee server started on port') == 1

		cleanup:
		project.gradle.sharedServices.registrations.nokeeServer.service.get().close()
	}

	static class LogHandler extends Handler {
		final def outputLines = []

		String getOutput() {
			return outputLines.join('\n')
		}

		@Override
		void publish(LogRecord logRecord) {
			outputLines.add(logRecord.message)
		}

		@Override
		void flush() {

		}

		@Override
		void close() throws SecurityException {

		}
	}
}
