/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.jni.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.internal.testing.NativeServicesTestFixture
import dev.nokee.platform.base.Variant
import dev.nokee.platform.jni.*
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary
import groovy.transform.Canonical
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.jvm.tasks.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder

trait JniLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.jni-library'
	}

	void applyPluginUnderTest() {
		applyPlugin()
	}

	void applyPlugin() {
		projectUnderTest.apply plugin: pluginId
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	void applyPluginAndEvaluate(String because) {
		applyPlugin()
		evaluateProject(because)
	}

	void resolveAllVariants(String because) {
		projectUnderTest.library.variants.elements.get()
	}
}

abstract class AbstractJniLibraryPluginSpec extends Specification {}

@Subject(JniLibraryPlugin)
class JniLibraryPluginLayoutTest extends AbstractPluginTest implements JniLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId

	@Override
	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	@Override
	Class getExtensionType() {
		return JavaNativeInterfaceLibrary
	}

	@Override
	Class getDependenciesType() {
		return JavaNativeInterfaceLibraryComponentDependencies
	}

	Class getVariantDependenciesType() {
		return JavaNativeInterfaceNativeComponentDependencies
	}
}

@Subject(JniLibraryPlugin)
class JniLibraryTaskPluginTest extends AbstractTaskPluginTest implements JniLibraryPluginTestFixture {
	@Override
	String[] getExpectedVariantAwareTaskNames() {
		return [
			'jar', /* JVM lifecycle */
			'sharedLibrary', /* native lifecycle */
		]
	}
}

@Subject(JniLibraryPlugin)
class JniLibraryVariantPluginTest extends AbstractVariantPluginTest implements JniLibraryPluginTestFixture {
	@Override
	def getExtensionUnderTest() {
		return project.library
	}

	@Override
	Class<? extends Variant> getVariantType() {
		return JniLibrary
	}
}

// TODO: Write test variant for no language and JVM/native mix
@Subject(JniLibraryPlugin)
class JniLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements JniLibraryPluginTestFixture {
	@Override
	def getExtensionUnderTest() {
		return project.library
	}

	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 2
			assert binaries.any { it instanceof JniJarBinary }
			assert binaries.any { it instanceof SharedLibraryBinary }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		if (extension.targetMachines.get().size() == 1) {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 2
				assert binaries.count { it instanceof JvmJarBinary } == 0
				assert binaries.count { it instanceof JniJarBinary } == 1
				assert binaries.count { it instanceof SharedLibraryBinary } == 1
			}
		} else {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 6
				assert binaries.count { it instanceof JvmJarBinary } == 0
				assert binaries.count { it instanceof JniJarBinary } == 3
				assert binaries.count { it instanceof SharedLibraryBinary } == 3
			}
		}
		return true
	}

	@Override
	void configureMultipleVariants() {
		extensionUnderTest.targetMachines = [extensionUnderTest.machines.macOS, extensionUnderTest.machines.windows, extensionUnderTest.machines.linux]
	}
}

@Subject(JniLibraryPlugin)
class JniLibraryPluginTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
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

	@Ignore // until refactoring is completed
	def "resolve variants when unrealized configuration are resolved"() {
		given:
		applyPlugin()
		def configuredVariants = []
		project.library.variants.configureEach {
			configuredVariants << it
			project.configurations.create('foo')
		}

		when:
		evaluateProject('plugin registers variants in afterEvaluate and register the configuration rule for on demand realization')
		then: 'nothing is realized initially'
		configuredVariants == []

		when:
		project.configurations.getByName('nativeRuntimeOnly')
		then: 'nothing is realized for base lifecycle configuration'
		configuredVariants == []

		when:
		project.configurations.getByName('foo')
		then: 'variants are realized for incoming configuration'
		!configuredVariants.isEmpty()
	}

	def "can access shared library from variant"() {
		given:
		applyPluginAndEvaluate('plugin registers variants in afterEvaluate')

		expect:
		def variant = one(project.library.variants.elements.get())
		variant.sharedLibrary instanceof SharedLibraryBinary
		def capturedBinaryDsl = null
		variant.sharedLibrary {
			capturedBinaryDsl = it
		}
		capturedBinaryDsl instanceof SharedLibraryBinary
	}

	def "can access tasks for each binaries"() {
		given:
		applyPluginAndEvaluate('plugin registers variants in afterEvaluate')

		expect:
		def variant = one(project.library.variants.elements.get())
		def binaries = variant.binaries.elements.get()
		binaries.size() == 2
		binaries[0].linkTask.get() instanceof LinkSharedLibrary
		binaries[1].jarTask.get() instanceof Jar
	}

	def "variants has target machines"() {
		given:
		applyPlugin()
		project.library {
			targetMachines = [machines.macOS.x86_64, machines.linux.x86]
		}
		evaluateProject('plugin register variants in afterEvaluate')

		expect:
		def variants = project.library.variants.elements.get()
		variants.size() == 2
		variants[0].targetMachine.operatingSystemFamily.macOS
		variants[0].targetMachine.architecture.'64Bit'

		and:
		variants[1].targetMachine.operatingSystemFamily.linux
		variants[1].targetMachine.architecture.'32Bit'
	}
	// TODO: Migrate test about each binaries available on the extension and each variant

	// TODO: as a well behaving plugin test, check mixing with other language
	// TODO: check that no tasks are created when variants are realized
}

class JniLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements JniLibraryPluginTestFixture {
	@Override
	def getExtensionNameUnderTest() {
		return 'library'
	}
}

class JniLibraryPluginWithIncompatiblePluginsTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
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

	@Unroll
	def "disallows applying objective-c software model plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'dev.nokee.objective-c-language' plugin instead of the 'objective-c' and 'objective-c-lang' plugins [1][2]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.objective-c-language' plugin, visit https://nokee.dev/docs/objective-c-language-plugin
			|[2] To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['objective-c', 'objective-c-lang'], 'dev.nokee.jni-library')
	}

	@Unroll
	def "disallows applying objective-cpp software model plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'dev.nokee.objective-cpp-language' plugin instead of the 'objective-cpp' and 'objective-cpp-lang' plugins [1][2]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.objective-cpp-language' plugin, visit https://nokee.dev/docs/objective-cpp-language-plugin
			|[2] To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['objective-cpp', 'objective-cpp-lang'], 'dev.nokee.jni-library')
	}

	@Unroll
	def "disallows applying cpp software model plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'dev.nokee.cpp-language' plugin instead of the 'cpp' and 'cpp-lang' plugins [1][2]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.cpp-language' plugin, visit https://nokee.dev/docs/cpp-language-plugin
			|[2] To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['cpp', 'cpp-lang'], 'dev.nokee.jni-library')
	}

	@Unroll
	def "disallows applying c software model plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'dev.nokee.c-language' plugin instead of the 'c' and 'c-lang' plugins [1][2]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.c-language' plugin, visit https://nokee.dev/docs/c-language-plugin
			|[2] To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['c', 'c-lang'], 'dev.nokee.jni-library')
	}

	@Unroll
	def "disallows applying new cpp plugins before nokee plugin"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'dev.nokee.cpp-language' plugin instead of the 'cpp-application' and 'cpp-library' plugins [1][2][3]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.cpp-language' plugin, visit https://nokee.dev/docs/cpp-language-plugin
			|[2] To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points
			|[3] To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugins
			|'''.stripMargin()

		where:
		disallowedPluginMix << [
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['cpp-application', 'dev.nokee.jni-library']),
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['cpp-library', 'dev.nokee.jni-library'])
		]
	}

	@Unroll
	def "disallows applying new swift plugins before nokee plugin"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following actions:
			| * Remove 'swift-application' and 'swift-library' plugins from the project [1]
			| * Vote on https://github.com/nokeedev/gradle-native/issues/26 issue to show interest for Swift language support
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points
			|'''.stripMargin()

		where:
		disallowedPluginMix << [
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['swift-application', 'dev.nokee.jni-library']),
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['swift-library', 'dev.nokee.jni-library'])
		]
	}

	@Unroll
	def "disallows applying new native plugins after nokee plugin"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following actions:
			| * Use 'dev.nokee.cpp-language' plugin instead of the 'cpp-application' and 'cpp-library' plugins [1][2][3]
			| * Remove 'swift-application' and 'swift-library' plugins from the project [2]
			| * Vote on https://github.com/nokeedev/gradle-native/issues/26 issue to show interest for Swift language support
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about 'dev.nokee.cpp-language' plugin, visit https://nokee.dev/docs/cpp-language-plugin
			|[2] To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points
			|[3] To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugins
			|'''.stripMargin()

		where:
		disallowedPluginMix << [
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['dev.nokee.jni-library', 'cpp-application']),
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['dev.nokee.jni-library', 'cpp-library']),
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['dev.nokee.jni-library', 'swift-application']),
			new DisallowPluginMixingSpec('dev.nokee.jni-library', ['dev.nokee.jni-library', 'swift-library'])
		]
	}

	@Unroll
	def "disallows applying Java application entry point plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Refer to https://nokee.dev/docs/building-jni-application for learning how to build JNI application [1]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['application'], 'dev.nokee.jni-library')
	}

	@Unroll
	def "disallows applying Java library entry point plugins"(disallowedPluginMix) {
		when:
		disallowedPluginMix.applyPluginsTo(project)

		then:
		def ex = thrown(PluginApplicationException)
		println ex.message
		println ex.cause.message

		and:
		ex.message == "Failed to apply plugin [${disallowedPluginMix.failingFullyQualifiedPluginId}]"

		and:
		ex.cause instanceof GradleException
		ex.cause.message == '''Nokee detected the usage of incompatible plugins in the project ':'.
			|We recommend taking the following action:
			| * Use 'java' plugin instead of 'java-library' plugin [1]
			|
			|To learn more, visit https://nokee.dev/docs/incompatible-plugins
			|
			|[1] To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points
			|'''.stripMargin()

		where:
		disallowedPluginMix << collectDisallowedPluginMixingPermutation(['java-library'], 'dev.nokee.jni-library')
	}

	private static List<DisallowPluginMixingSpec> collectDisallowedPluginMixingPermutation(List<String> disallowedPluginIds, String pluginIdUnderTest) {
		return disallowedPluginIds.collect { disallowedPluginId ->
			collectEachPermutation([disallowedPluginId, pluginIdUnderTest]).collect { disallowedPluginMixture ->
				new DisallowPluginMixingSpec(pluginIdUnderTest, disallowedPluginMixture)
			}
		}.flatten()
	}

	private static List<String> getSoftwareModelPluginIds() {
		return ['cpp', 'cpp-lang', 'c', 'c-lang', 'objective-c', 'objective-c-lang', 'objective-cpp', 'objective-cpp-lang']
	}

	private static List<String> getCurrentModelPluginIds() {
		return ['cpp-application', 'cpp-library', 'swift-application', 'swift-library']
	}

	@Canonical
	private static class DisallowPluginMixingSpec {
		String pluginIdUnderTest
		List<String> pluginMixture

		DisallowPluginMixingSpec(String pluginIdUnderTest, List<String> pluginMixture) {
			assert pluginMixture.size() == 2
			this.pluginIdUnderTest = pluginIdUnderTest
			this.pluginMixture = pluginMixture
		}

		String getFailingFullyQualifiedPluginId() {
			String result = pluginMixture.last()
			if (result.contains('.')) {
				return "id '${result}'"
			} else if (softwareModelPluginIds.contains(result)) {
				if (result.endsWith('-lang')) {
					return "id 'org.gradle.${result}'"
				}
				def tokens = result.split('-')
				return "class 'org.gradle.language.${tokens.join('')}.plugins.${tokens.collect { it.capitalize() }.join('')}LangPlugin'"
			} else if (currentModelPluginIds.contains(result)) {
				return "class 'org.gradle.language.plugins.NativeBasePlugin'"
			}
			return "id 'org.gradle.${result}'"
		}

		void applyPluginsTo(Project projectUnderTest) {
			pluginMixture.each {
				println "Applying plugin '$it'"
				projectUnderTest.apply plugin: it
			}
		}
	}

	private static List<List<String>> collectEachPermutation(List<String> values) {
		def result = []
		values.eachPermutation {
			result << it
		}
		return result
	}
}

@Subject(JniLibraryPlugin)
class JniLibraryPluginVariantAwareTaskDescriptionTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
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

	// This test should ensure the description are done properly for all variant aware task
	// No target machines -> build for the current host
	// Explicit OS -> build for the os but current host architecture
	// Explicit OS & architecture -> build for the ost and architecture specified
	// TODO: Publishing should probably only occurs IF the target machine is EXPLICIT
}

@Subject(JniLibraryPlugin)
class JniLibraryPluginWithNoLanguageTasksTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
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

	def "creates variant-aware tasks for ambiguous operating system family dimension"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.macOS, machines.linux]
		}
		evaluateProject('plugin registers lifecycle tasks in afterEvaluate')
		resolveAllVariants('plugin creates some tasks on demand')

		then:
		tasks*.name as Set == [
			'jarMacos', 'jarLinux', /* JVM lifecycle */
			'sharedLibraryMacos', 'sharedLibraryLinux', /* native lifecycle */
			'linkMacos', 'linkLinux', /* native link tasks */
			'assemble', 'assembleMacos', 'assembleLinux', 'clean', 'build', 'check' /* general lifecycle */
		] as Set
	}

	def "creates variant-aware tasks for ambiguous architecture dimension"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.windows.x86, machines.windows.x86_64]
		}
		evaluateProject('plugin registers lifecycle tasks in afterEvaluate')
		resolveAllVariants('plugin creates some tasks on demand')

		then:
		tasks*.name as Set == [
			'jarX86', 'jarX86-64', /* JVM lifecycle */
			'sharedLibraryX86', 'sharedLibraryX86-64', /* native lifecycle */
			'linkX86', 'linkX86-64', /* native link tasks */
			'assemble', 'assembleX86', 'assembleX86-64', 'clean', 'build', 'check' /* general lifecycle */
		] as Set
	}

	def "creates variant-aware tasks for ambiguous operating system family and architecture dimensions"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.windows.x86, machines.macOS.x86_64]
		}
		evaluateProject('plugin registers lifecycle tasks in afterEvaluate')
		resolveAllVariants('plugin creates some tasks on demand')

		then:
		tasks*.name as Set == [
			'jarWindowsX86', 'jarMacosX86-64', /* JVM lifecycle */
			'sharedLibraryWindowsX86', 'sharedLibraryMacosX86-64', /* native lifecycle */
			'linkWindowsX86', 'linkMacosX86-64', /* native link tasks */
			'assemble', 'assembleWindowsX86', 'assembleMacosX86-64', 'clean', 'build', 'check' /* general lifecycle */
		] as Set
	}

	def "jar task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.jar.group == 'build'
		project.tasks.jar.description == 'Assembles a jar archive containing the shared library.'
	}

	def "sharedLibrary task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.sharedLibrary.group == 'build'
		project.tasks.sharedLibrary.description == 'Assembles a shared library binary containing the main objects.'
	}

	def "clean task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.clean.group == 'build'
		project.tasks.clean.description == 'Deletes the build directory.'
	}

	def "build task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.build.group == 'build'
		project.tasks.build.description == 'Assembles and tests this project.'
	}

	def "check task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.check.group == 'verification'
		project.tasks.check.description == 'Runs all checks.'
	}

	def "link task has no group and a description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')
		resolveAllVariants('plugin creates some tasks on demand')

		expect:
		project.tasks.link.group == null
		project.tasks.link.description == 'Links the shared library.'
	}
}

// TODO: Check that each native language adds native compile tasks & group/description
