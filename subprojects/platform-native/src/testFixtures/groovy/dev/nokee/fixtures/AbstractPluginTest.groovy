package dev.nokee.fixtures

import dev.nokee.platform.base.DependencyAwareComponent
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantAwareComponent
import dev.nokee.platform.base.VariantView
import dev.nokee.platform.nativebase.NativeComponentDependencies
import dev.nokee.platform.nativebase.NativeLibraryDependencies
import dev.nokee.platform.nativebase.TargetMachineAwareComponent
import dev.nokee.platform.nativebase.TargetMachineFactory
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractPluginTest extends Specification implements ProjectTestFixture {
	def project

	def setup() {
		if (SystemUtils.IS_OS_WINDOWS) {
			NativeServicesTestFixture.initialize()
		}
		project = project = ProjectBuilder.builder().withName('proj').build()
	}

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

	Class getVariantDependenciesType() {
		return getDependenciesType()
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

	def "extensions has variant view"() {
		given:
		applyPluginUnderTest()

		expect: 'extension has public variant api'
		extensionUnderTest instanceof VariantAwareComponent

		and: 'variant getter is of the expected type'
		extensionUnderTest.variants instanceof VariantView
	}

	def "variants has dependencies dsl"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin registers variants in afterEvaluate')
		def variants = extensionUnderTest.variants.get()

		expect: 'variant has public dependencies api'
		variants.every { it instanceof DependencyAwareComponent }

		and: 'dependency getter is of the expected type'
		variants.every { getVariantDependenciesType().isAssignableFrom(it.dependencies.getClass()) }

		and: 'dependency block is of the expected type'
		variants.every { variant ->
			def capturedDependencyDsl = null
			variant.dependencies {
				capturedDependencyDsl = it
			}
			return getVariantDependenciesType().isAssignableFrom(capturedDependencyDsl.getClass())
		}
	}
}

abstract class AbstractTargetMachineAwarePluginTest extends Specification implements ProjectTestFixture {
	def project

	def setup() {
		if (SystemUtils.IS_OS_WINDOWS) {
			NativeServicesTestFixture.initialize()
		}
		project = project = ProjectBuilder.builder().withName('test').build()
	}

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

abstract class AbstractTaskPluginTest extends Specification implements ProjectTestFixture {
	def project

	def setup() {
		if (SystemUtils.IS_OS_WINDOWS) {
			NativeServicesTestFixture.initialize()
		}
		project = project = ProjectBuilder.builder().withName('lib').build()
	}

	@Override
	Project getProjectUnderTest() {
		return project
	}

	abstract void applyPluginUnderTest()

	abstract String[] getExpectedVariantAwareTaskNames();

	def "creates lifecycle tasks"() {
		when:
		applyPluginUnderTest()
		evaluateProject('plugin registers lifecycle tasks in afterEvaluate')

		then:
		tasks*.name as Set == [
			*expectedVariantAwareTaskNames,
			'assemble', 'clean', 'build', 'check' /* general lifecycle */
		] as Set
	}
}

abstract class AbstractVariantPluginTest extends Specification implements ProjectTestFixture {
	def project

	def setup() {
		if (SystemUtils.IS_OS_WINDOWS) {
			NativeServicesTestFixture.initialize()
		}
		project = project = ProjectBuilder.builder().withName('test').build()
	}

	@Override
	Project getProjectUnderTest() {
		return project
	}

	abstract void applyPluginUnderTest()

	abstract def getExtensionUnderTest()

	abstract Class<? extends Variant> getVariantType()

	def "variants are of the expected type"() {
		when:
		applyPluginUnderTest()
		evaluateProject('plugin registers variants in afterEvaluate')

		then:
		extensionUnderTest.variants.get().every { variantType.isAssignableFrom(it.class) }
	}

	def "disallows variant view realization before evaluation"() {
		given:
		applyPluginUnderTest()

		when:
		extensionUnderTest.variants.elements.get()
		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing the variants.'
	}

	def "allow variant view realization after evaluation"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin locks variant view in afterEvaluate')

		when:
		extensionUnderTest.variants.elements.get()
		then:
		noExceptionThrown()
	}
}
