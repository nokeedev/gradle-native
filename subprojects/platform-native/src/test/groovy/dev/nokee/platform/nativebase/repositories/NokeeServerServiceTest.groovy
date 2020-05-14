package dev.nokee.platform.nativebase.repositories

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.nokee.platform.nativebase.internal.plugins.FakeMavenRepositoryPlugin
import dev.nokee.platform.nativebase.internal.repositories.NokeeServerService
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.StandardOutputListener
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject
import spock.util.environment.OperatingSystem

import java.util.logging.Handler
import java.util.logging.Level
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
		project.gradle.sharedServices.registrations.nokeeServer.service.get() // Force start

		then:
		noExceptionThrown()

		cleanup:
		takeOverTheHardCodedPort.close()
		project.gradle.sharedServices.registrations.nokeeServer.service.get().close()
	}

	def "does not start a server when not trying to resolve from the Nokee repository"() {
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
		log.output.count('Nokee server started on port') == 0
	}

	def "only start one server when trying to resolve from the Nokee repository"() {
		given:
		LogHandler log = new LogHandler()
		LogManager.logManager.getLogger("").addHandler(log)
		def rootProject = ProjectBuilder.builder().withProjectDir(temporaryFolder.testDirectory).withName('root').build()
		def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.createDirectory('subproject')).withParent(rootProject).build()

		and:
		rootProject.apply plugin: FakeMavenRepositoryPlugin
		project.apply plugin: FakeMavenRepositoryPlugin

		and: 'a configuration that will be resolved by the local repository'
		project.repositories.getByName(NokeeServerService.NOKEE_LOCAL_REPOSITORY_NAME).mavenContent {
			includeGroup('dev.nokee.heartbeat')
		}
		def foo = project.configurations.create('foo')
		foo.dependencies.add(project.dependencies.create("dev.nokee.heartbeat:heartbeat:latest.integration"))

		when: 'it is resolved'
		foo.resolvedConfiguration.lenientConfiguration.each {}

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
