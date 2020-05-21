package dev.nokee.fixtures

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractPluginTest extends Specification implements ProjectTestFixture {
	def project = ProjectBuilder.builder().withName('proj').build()

	Project getProjectUnderTest() {
		return project
	}

	abstract void applyPluginUnderTest()
	abstract def getExtensionUnderTest()
	abstract Class getExtensionType()

	def "registers extension on project"() {
		when:
		applyPluginUnderTest()

		then:
		extensionUnderTest != null
		getExtensionType().isAssignableFrom(extensionUnderTest.getClass())
	}
}
