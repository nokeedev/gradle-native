package dev.nokee.platform.jni.internal.plugins

import dev.nokee.platform.jni.JniLibraryExtension
import dev.nokee.platform.nativebase.TargetMachineFactory
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory
import groovy.transform.Canonical
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

trait ProjectTestFixture {
	abstract Project getProjectUnderTest()

	boolean hasTask(String name) {
		return projectUnderTest.tasks.findByName(name) != null
	}

	boolean hasConfiguration(String name) {
		return projectUnderTest.configurations.findByName(name) != null
	}

	Set<Configuration> getConfigurations() {
		return projectUnderTest.configurations.findAll { !['archives', 'default'].contains(it.name) }
	}

	Set<Task> getTasks() {
		return projectUnderTest.tasks.findAll { !['help', 'tasks', 'components', 'dependencies', 'dependencyInsight', 'dependentComponents', 'init', 'model', 'outgoingVariants', 'projects', 'properties', 'wrapper', 'buildEnvironment'].contains(it.name) }
	}
}

trait JniLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPlugin() {
		projectUnderTest.apply plugin: 'dev.nokee.jni-library'
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

@Subject(JniLibraryPlugin)
abstract class AbstractJniLibraryPluginSpec extends Specification {}

@Subject(JniLibraryPlugin)
class JniLibraryPluginTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
	def project = ProjectBuilder.builder().withName('lib').build()

	@Override
	Project getProjectUnderTest() {
		return project
	}

	def "registers extension on project"() {
		when:
		applyPlugin()

		then:
		project.library != null
		project.library instanceof JniLibraryExtension
	}

	@Ignore() // TODO: Fix this before 0.4
	def "disallows query of views before evaluation"() {
		given:
		applyPlugin()

		when:
		project.library.binaries.elements.get()
		then:
		def ex1 = thrown(IllegalStateException)
		ex1.message == ''

		when:
		project.library.variants.elements.get()
		then:
		def ex2 = thrown(IllegalStateException)
		ex2.message == ''
	}

	def "allow query of views after evaluation"() {
		given:
		applyPluginAndEvaluate('plugin locks views in afterEvaluate')

		when:
		def binaries = project.library.binaries.elements.get()
		then:
		noExceptionThrown()
		and:
		binaries.size() == 2

		when:
		def variants = project.library.variants.elements.get()
		then:
		noExceptionThrown()
		and:
		variants.size() == 1
	}

	// TODO: Check dependencies api of the right type on extension
	// TODO: Check dependencies api of the right type on each variants
	// TODO: Check easy access to shared library from each variants
	// TODO: Migrate test about each binaries available on the extension and each variant
	// TODO: Check each variant is of the right type and has the right target machine (platform) configured
	// TODO: Check binary tasks are not null and of the right type

	// TODO: as a well behaving plugin test, check mixing with other language
	// TODO: check that no tasks are created when variants are realized
}

class JniLibraryPluginTargetMachineConfigurationTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
	def project = ProjectBuilder.builder().withName('lib').build()

	@Override
	Project getProjectUnderTest() {
		return project
	}

	def "disallows modification to target machine after evaluation"() {
		given:
		applyPlugin()

		when:
		project.library {
			targetMachines = [machines.windows]
		}
		then:
		noExceptionThrown()

		when:
		evaluateProject('plugin lock target machines in afterEvaluate')
		project.library {
			targetMachines = [machines.macOS]
		}
		then:
		def ex = thrown(IllegalStateException)
		ex.message == "The value for property 'targetMachines' is final and cannot be changed any further."
	}

	def "disallows empty target machines list"() {
		given:
		applyPlugin()

		when:
		project.library.targetMachines = []
		evaluateProject('plugin resolve target machines in afterEvaluate')

		then:
		def ex = thrown(ProjectConfigurationException)
		ex.message == "A problem occurred configuring root project 'lib'."
		ex.cause instanceof IllegalArgumentException
		ex.cause.message == 'A target machine needs to be specified for the library.'
	}

	def "can reset target machines to host by setting to null"() {
		given:
		applyPlugin()

		when:
		project.library {
			targetMachines = [machines.os('foo')]
			targetMachines = null
		}
		evaluateProject('plugin resolve target machines in afterEvaluate')

		then:
		noExceptionThrown()

		and:
		project.library.targetMachines.get() == [DefaultTargetMachineFactory.host()] as Set
	}

	def "extensions has target machine factory"() {
		given:
		applyPlugin()

		expect:
		project.library.machines instanceof TargetMachineFactory
	}
}

class JniLibraryPluginWithIncompatiblePluginsTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
	def project = ProjectBuilder.builder().withName('lib').build()

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
			|[3] To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugin
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
			|[3] To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugin
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
	def project = ProjectBuilder.builder().withName('lib').build()

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
	def project = ProjectBuilder.builder().withName('lib').build()

	@Override
	Project getProjectUnderTest() {
		return project
	}

	def "creates lifecycle tasks"() {
		when:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		then:
		tasks*.name as Set == [
			'jar', /* JVM lifecycle */
			'sharedLibrary', /* native lifecycle */
			'assemble', 'clean', 'build', 'check' /* general lifecycle */
		] as Set
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

	def "assemble task has group and description"() {
		given:
		applyPluginAndEvaluate('plugin registers lifecycle tasks in afterEvaluate')

		expect:
		project.tasks.assemble.group == 'build'
		project.tasks.assemble.description == 'Assembles the outputs of this project.'
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

@Subject(JniLibraryPlugin)
class JniLibraryPluginWithNoLanguageConfigurationsTest extends AbstractJniLibraryPluginSpec implements ProjectTestFixture, JniLibraryPluginTestFixture {
	def project = ProjectBuilder.builder().withName('lib').build()

	@Override
	Project getProjectUnderTest() {
		return project
	}

	def "creates buckets, JVM outgoing, native incoming for linking and runtime configurations"() {
		when:
		applyPluginAndEvaluate('plugin registers variants in afterEvaluate')
		resolveAllVariants('plugin creates configurations on demand')

		then:
		configurations*.name as Set == [
			'api', 'jvmImplementation', 'jvmRuntimeOnly', /* JVM buckets */
			'apiElements', 'runtimeElements', /* JVM outgoing */
			'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly', /* native buckets */
			'nativeLinkLibraries', 'nativeRuntimeLibraries' /* native incoming */
		] as Set
	}

	def "creates variant-aware configurations for ambiguous operating system family dimension"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.macOS, machines.linux]
		}
		evaluateProject('plugin register variants in afterEvaluate')
		resolveAllVariants('plugin creates configuration on demand')

		then:
		configurations*.name as Set == [
			'api', 'jvmImplementation', 'jvmRuntimeOnly', /* JVM buckets */
			'apiElements', 'runtimeElements', /* JVM outgoing */
			'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly', /* general native buckets */
			'macosNativeImplementation', 'macosNativeLinkOnly', 'macosNativeRuntimeOnly', /* macOS buckets */
			'linuxNativeImplementation', 'linuxNativeLinkOnly', 'linuxNativeRuntimeOnly', /* linux buckets */
			'macosNativeLinkLibraries', 'macosNativeRuntimeLibraries', /* macOS incoming */
			'linuxNativeLinkLibraries', 'linuxNativeRuntimeLibraries' /* linux incoming */
		] as Set
	}

	def "creates variant-aware configurations for ambiguous architecture dimension"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.windows.x86, machines.windows.x86_64]
		}
		evaluateProject('plugin register variants in afterEvaluate')
		resolveAllVariants('plugin creates configuration on demand')

		then:
		configurations*.name as Set == [
			'api', 'jvmImplementation', 'jvmRuntimeOnly', /* JVM buckets */
			'apiElements', 'runtimeElements', /* JVM outgoing */
			'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly', /* general native buckets */
			'x86NativeImplementation', 'x86NativeLinkOnly', 'x86NativeRuntimeOnly', /* x86 buckets */
			'x86-64NativeImplementation', 'x86-64NativeLinkOnly', 'x86-64NativeRuntimeOnly', /* x86-64 buckets */
			'x86NativeLinkLibraries', 'x86NativeRuntimeLibraries', /* x86 incoming */
			'x86-64NativeLinkLibraries', 'x86-64NativeRuntimeLibraries' /* x86-64 incoming */
		] as Set
	}

	def "creates variant-aware configurations for ambiguous operating system family and architecture dimensions"() {
		when:
		applyPlugin()
		project.library {
			targetMachines = [machines.windows.x86, machines.macOS.x86_64]
		}
		evaluateProject('plugin register variants in afterEvaluate')
		resolveAllVariants('plugin creates configuration on demand')

		then:
		configurations*.name as Set == [
			'api', 'jvmImplementation', 'jvmRuntimeOnly', /* JVM buckets */
			'apiElements', 'runtimeElements', /* JVM outgoing */
			'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly', /* general native buckets */
			'windowsX86NativeImplementation', 'windowsX86NativeLinkOnly', 'windowsX86NativeRuntimeOnly', /* windowsX86 buckets */
			'macosX86-64NativeImplementation', 'macosX86-64NativeLinkOnly', 'macosX86-64NativeRuntimeOnly', /* macosX86-64 buckets */
			'windowsX86NativeLinkLibraries', 'windowsX86NativeRuntimeLibraries', /* windowsX86 incoming */
			'macosX86-64NativeLinkLibraries', 'macosX86-64NativeRuntimeLibraries' /* macosX86-64 incoming */
		] as Set
	}

	@Ignore
	def "JVM configurations has description"() {
		given:
		applyPluginAndEvaluate('plugin registers variants in afterEvaluate')
		resolveAllVariants('plugin creates configurations on demand')

		expect:
		project.configurations.api.description == ''
		project.configurations.jvmImplementation.description == ''
		project.configurations.jvmRuntimeOnly.description == ''
		project.configurations.apiElements.description == ''
		project.configurations.runtimeElements.description == ''
	}

	@Ignore
	def "native configurations has description"() {
		given:
		applyPluginAndEvaluate('plugin registers variants in afterEvaluate')
		resolveAllVariants('plugin creates configurations on demand')

		expect:
		project.configurations.nativeImplementation.description == 'Implementation only dependencies for JNI shared library.'
		project.configurations.nativeLinkOnly.description == 'Link only dependencies for JNI shared library.'
		project.configurations.nativeRuntimeOnly.description == 'Runtime only dependencies for JNI shared library.'
		project.configurations.nativeLinkLibraries.description == ''
		project.configurations.nativeRuntimeLibraries.description == ''
	}

	def "resolve variants when unrealized configuration are resolved"() {
		given:
		applyPlugin()
		def configuredVariants = []
		project.library.variants.configureEach { configuredVariants << it }

		when:
		evaluateProject('plugin registers variants in afterEvaluate and register the configuration rule for on demand realization')
		then: 'nothing is realized initially'
		configuredVariants == []

		when:
		project.configurations.getByName('nativeRuntimeOnly')
		then: 'nothing is realized for base lifecycle configuration'
		configuredVariants == []

		when:
		project.configurations.getByName('nativeRuntimeLibraries')
		then: 'variants are realized for incoming configuration'
		!configuredVariants.isEmpty()
	}
}

// TODO: Check that each native language adds compile configuration & description
