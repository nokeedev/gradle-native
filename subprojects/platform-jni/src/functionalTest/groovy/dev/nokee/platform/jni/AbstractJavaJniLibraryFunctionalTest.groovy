package dev.nokee.platform.jni


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.sources.SourceElement

abstract class AbstractJavaJniLibraryFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private static collectEachPermutation(values) {
		def result = []
		values.eachPermutation {
			result << it
		}
		return result
	}

	protected static String configurePlugins(List<String> pluginIds) {
		return """
			plugins {
				${pluginIds.collect { "id '$it'"}.join('\n')}
			}
		"""
	}

	def "can apply plugins in what ever order"(pluginIds) {
		given:
		settingsFile << "rootProject.name = 'library'"
		buildFile << configurePlugins(pluginIds)

		when:
		succeeds('assemble')

		then:
		jar('build/libs/library.jar')

		where:
		pluginIds << collectEachPermutation(['java', 'dev.nokee.jni-library', 'dev.nokee.c-language'])
	}

	def "produce a single JAR containing the shared library for single variant"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		file('build/libs').assertHasDescendants(sharedLibraryName('main/shared/jni-greeter'), 'jni-greeter.jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	def "generate JNI headers when compiling Java source code"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		file('build/generated/jni-headers').assertDoesNotExist()
		succeeds('compileJava')

		then:
		file('build/generated/jni-headers').assertHasDescendants('com_example_greeter_Greeter.h')
	}

	def "build logic can change build directory location"() {
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        given:
        buildFile << '''
            buildDir = 'output'
         '''

        expect:
        succeeds 'assemble'

        !file('build').exists()
		file('output/libs').assertHasDescendants(sharedLibraryName('main/shared/jni-greeter'), 'jni-greeter.jar')
		jar("output/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
    }

	def "build fails when Java compilation fails"() {
		given:
		makeSingleProject()

		and:
		file("src/main/java/broken.java") << "broken!"

		expect:
		fails "assemble"
		failure.assertHasDescription("Execution failed for task ':compileJava'.")
		failure.assertHasCause("Compilation failed; see the compiler error output for details.")
	}

	def "generates a empty JAR when no souce"() {
        given:
        makeSingleProject()

        expect:
        succeeds 'assemble'
		jar('build/libs/jni-greeter.jar').hasDescendants()
    }

	def "uses the project group as the default resource path for the shared library"() {
		makeSingleProject()
		buildFile << configureProjectGroup('com.example.greeter')
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/greeter/jni-greeter'))
	}

	def "can configure the resource path for each variant"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			library {
				variants.configureEach {
					resourcePath = 'com/example/foobar'
				}
			}
		"""

		when:
		succeeds('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/foobar/jni-greeter'))
	}

	protected String configureProjectGroup(String groupId) {
		return """
			group = '${groupId}'
		"""
	}

	protected abstract String getDevelopmentBinaryNativeCompileTask()

	protected abstract void makeSingleProject()

	abstract SourceElement getComponentUnderTest()
}
