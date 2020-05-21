package dev.nokee.fixtures

import dev.nokee.platform.base.DependencyAwareComponent
import dev.nokee.platform.nativebase.NativeComponentDependencies
import dev.nokee.platform.nativebase.NativeLibraryDependencies
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractPluginTest extends Specification implements ProjectTestFixture {
	def project = ProjectBuilder.builder().withName('proj').build()

	Project getProjectUnderTest() {
		return project
	}

	abstract String getPluginIdUnderTest()
	abstract void applyPluginUnderTest()
	abstract def getExtensionUnderTest()
	abstract Class getExtensionType()

	Class getDependenciesType() {
		if (pluginIdUnderTest.endsWith('library')) {
			return NativeLibraryDependencies
		}
		return NativeComponentDependencies
	}

	def "registers extension on project"() {
		when:
		applyPluginUnderTest()

		then:
		extensionUnderTest != null
		getExtensionType().isAssignableFrom(extensionUnderTest.getClass())
	}

	def "extensions has dependencies dsl"() {
		given:
		applyPluginUnderTest()

		expect: 'extension has public dependencies api'
		extensionUnderTest instanceof DependencyAwareComponent

		and: 'dependency getter is of the expected type'
		getDependenciesType().isAssignableFrom(extensionUnderTest.dependencies.getClass())

		and: 'dependency block is of the expected type'
		def capturedDependencyDsl = null
		extensionUnderTest.dependencies {
			capturedDependencyDsl = it
		}
		getDependenciesType().isAssignableFrom(capturedDependencyDsl.getClass())
	}
}
