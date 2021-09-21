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
package dev.nokee.runtime.base.internal.repositories

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject
import spock.util.environment.OperatingSystem

import java.util.logging.Handler
import java.util.logging.LogManager
import java.util.logging.LogRecord

@Subject(NokeeServerService)
@Requires({ OperatingSystem.current.macOs })
class NokeeServerServiceTest extends Specification {
	def "choose a free random port to listen on"() {
		given:
		def takeOverTheHardCodedPort = new ServerSocket(9666) // used to use a static port
		def project = ProjectTestUtils.rootProject()

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
		def rootProject = ProjectBuilder.builder().withName('root').build()
		def project = ProjectBuilder.builder().withParent(rootProject).build()

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
		def rootProject = ProjectBuilder.builder().withName('root').build()
		def project = ProjectBuilder.builder().withParent(rootProject).build()

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
		project.evaluate() // must evaluate the project first because of a Gradle regression
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
