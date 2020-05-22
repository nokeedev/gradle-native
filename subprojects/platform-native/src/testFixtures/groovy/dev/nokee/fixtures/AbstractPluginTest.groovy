package dev.nokee.fixtures

import dev.nokee.platform.base.DependencyAwareComponent
import dev.nokee.platform.nativebase.NativeComponentDependencies
import dev.nokee.platform.nativebase.NativeLibraryDependencies
import dev.nokee.platform.nativebase.TargetMachineAwareComponent
import dev.nokee.platform.nativebase.TargetMachineFactory
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
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

abstract class AbstractTargetMachineAwarePluginTest extends Specification implements ProjectTestFixture {
	def project = ProjectBuilder.builder().withName('test').build()

	@Override
	Project getProjectUnderTest() {
		return project
	}

	abstract void applyPluginUnderTest()

	abstract def getExtensionNameUnderTest()

	def "extensions is a target machine aware component"() {
		given:
		applyPluginUnderTest()

		expect:
		project."${extensionNameUnderTest}" instanceof TargetMachineAwareComponent
	}

	def "disallows modification to target machine after evaluation"() {
		given:
		applyPluginUnderTest()

		when:
		project."${extensionNameUnderTest}" {
			targetMachines = [machines.windows]
		}
		then:
		noExceptionThrown()

		when:
		evaluateProject('plugin lock target machines in afterEvaluate')
		project."${extensionNameUnderTest}" {
			targetMachines = [machines.macOS]
		}
		then:
		def ex = thrown(IllegalStateException)
		ex.message == "The value for property 'targetMachines' is final and cannot be changed any further."
	}

	def "disallows empty target machines list"() {
		given:
		applyPluginUnderTest()

		when:
		project."${extensionNameUnderTest}".targetMachines = []
		evaluateProject('plugin resolve target machines in afterEvaluate')

		then:
		def ex = thrown(ProjectConfigurationException)
		ex.message == "A problem occurred configuring root project 'test'."
		ex.cause instanceof IllegalArgumentException
		ex.cause.message == "A target machine needs to be specified for the ${extensionNameUnderTest}."
	}

	def "can reset target machines to host by setting to null"() {
		given:
		applyPluginUnderTest()

		when:
		project."${extensionNameUnderTest}" {
			targetMachines = [machines.os('foo')]
			targetMachines = null
		}
		evaluateProject('plugin resolve target machines in afterEvaluate')

		then:
		noExceptionThrown()

		and:
		project."${extensionNameUnderTest}".targetMachines.get() == [DefaultTargetMachineFactory.host()] as Set
	}

	def "extensions has target machine factory"() {
		given:
		applyPluginUnderTest()

		expect:
		project."${extensionNameUnderTest}".machines instanceof TargetMachineFactory
	}
}
