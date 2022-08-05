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
package dev.nokee.fixtures

import dev.nokee.internal.testing.NativeServicesTestFixture
import dev.nokee.platform.base.*
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies
import dev.nokee.platform.nativebase.TargetMachineAwareComponent
import dev.nokee.runtime.nativebase.TargetMachineFactory
import dev.nokee.runtime.nativebase.internal.TargetMachines
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
			return NativeLibraryComponentDependencies
		}
		return NativeApplicationComponentDependencies
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

	def "extensions has binary view"() {
		given:
		applyPluginUnderTest()

		expect: 'extension has public binary api'
		extensionUnderTest instanceof BinaryAwareComponent

		and: 'variant getter is of the expected type'
		extensionUnderTest.binaries instanceof BinaryView
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

	def "variants has binary view"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin registers variants in afterEvaluate')
		def variants = extensionUnderTest.variants.get()

		expect: 'variant has public dependencies api'
		variants.every { it instanceof BinaryAwareComponent }

		and: 'dependency getter is of the expected type'
		variants.every { it.binaries instanceof BinaryView }
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
		ex.message == "The value for ${project."${extensionNameUnderTest}"} property 'targetMachines' is final and cannot be changed any further."
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
		def exCause = ex.cause.cause == null ? ex.cause : ex.cause.cause
		exCause instanceof IllegalArgumentException
		exCause.message == "A target machine needs to be specified for component 'main'."//${extensionNameUnderTest}."
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
		project."${extensionNameUnderTest}".targetMachines.get() == [TargetMachines.host()] as Set
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
		(tasks*.name as Set).containsAll([
			*expectedVariantAwareTaskNames,
			'assemble', 'clean', 'build', 'check' /* general lifecycle */
		] as Set) // additional javaToolchains on later Gradle
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
}

abstract class AbstractBinaryPluginTest extends Specification implements ProjectTestFixture {
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

	def "allow binary view realization after evaluation"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin locks binary view in afterEvaluate')

		when:
		extensionUnderTest.binaries.elements.get()
		then:
		noExceptionThrown()
	}

	abstract boolean hasExpectedBinaries(Variant variant)
	abstract boolean hasExpectedBinaries(def extension)

	abstract void configureMultipleVariants()

	def "has expected default binaries per-variant"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin locks binary view in afterEvaluate')

		expect:
		def variants = extensionUnderTest.variants.get()
		variants.every { hasExpectedBinaries((Variant)it) }
	}

	def "has expected default binaries on extension"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin locks binary view in afterEvaluate')

		expect:
		hasExpectedBinaries(extensionUnderTest)
	}

	def "can configure each binaries via the component binary view"() {
		given:
		applyPluginUnderTest()

		and: 'register configuration action'
		def configured = false
		extensionUnderTest.binaries.configureEach {
			configured = true
		}

		and:
		evaluateProject('plugin locks binary view in afterEvaluate')

		when:
		extensionUnderTest.binaries.get()

		then:
		configured
	}

	def "can configure each binaries via the variant binary view"() {
		given:
		applyPluginUnderTest()

		and: 'register configuration action'
		def configured = false
		extensionUnderTest.variants.configureEach {
			binaries.configureEach {
				configured = true
			}
		}

		and:
		evaluateProject('plugin locks binary view in afterEvaluate')

		when:
		extensionUnderTest.binaries.get()

		then:
		configured
	}

	def "uses the same binary instance in both component and variant binary view"() {
		given:
		applyPluginUnderTest()
		evaluateProject('plugin locks binary view in afterEvaluate')

		expect:
		extensionUnderTest.binaries.get() == one(extensionUnderTest.variants.get()).binaries.get()
	}

	def "aggregates binaries from each variant in the component binary view"() {
		given:
		applyPluginUnderTest()
		configureMultipleVariants()
		evaluateProject('plugin locks binary view in afterEvaluate')

		expect:
		hasExpectedBinaries(extensionUnderTest)

		and:
		def variants = extensionUnderTest.variants.get()
		variants.every { hasExpectedBinaries((Variant)it) }
	}
}
