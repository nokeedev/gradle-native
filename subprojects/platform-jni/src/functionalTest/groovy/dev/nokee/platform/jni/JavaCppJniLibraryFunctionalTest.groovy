package dev.nokee.platform.jni


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

import static org.hamcrest.CoreMatchers.containsString

class JavaCppJniLibraryFunctionalTest extends AbstractInstalledToolChainIntegrationSpec implements MixedLanguageTaskNames {
	def "can apply plugins in what ever order"(pluginIds) {
		given:
		settingsFile << "rootProject.name = 'library'"
		buildFile << configurePlugins(pluginIds)

		when:
		succeeds('assemble')

		then:
		jar('build/libs/library.jar')

		where:
		pluginIds << collectEachPermutation(['java', 'dev.nokee.jni-library', 'dev.nokee.cpp-language'])
	}

	private static collectEachPermutation(values) {
		def result = []
		values.eachPermutation {
			result << it
		}
		return result
	}

	def "generates a empty JAR when no souce"() {
        given:
        makeSingleProject()

        expect:
        succeeds 'assemble'
		jar('build/libs/jni-greeter.jar').hasDescendants()
    }

    def "build fails when C++ compilation fails"() {
        given:
        makeSingleProject()

        and:
        file("src/main/cpp/broken.cpp") << "broken!"

        expect:
        fails "assemble"
        failure.assertHasDescription("Execution failed for task '$developmentBinaryNativeCompileTask'.")
        failure.assertHasCause("A build operation failed.")
        failure.assertThatCause(containsString("C++ compiler failed while compiling broken.cpp"))
    }

    def "build fails when Java compilation fails"() {
        given:
        makeSingleProject()

        and:
        file("src/main/java/broken.java") << "broken!"

        expect:
        fails "assemble"
        failure.assertHasDescription("Execution failed for task '$developmentBinaryJvmCompileTask'.")
        failure.assertHasCause("Compilation failed; see the compiler error output for details.")
    }

    def "produce a single JAR containing the shared library for single variant"() {
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':assemble')
        result.assertTasksSkipped(taskNames.java.tasks.processResources)

		file('build/libs').assertHasDescendants(sharedLibraryName('main/shared/jni-greeter'), 'jni-greeter.jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
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
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':assemble')

        !file('build').exists()
        file('output/objs/main/shared/mainCpp').assertIsDirectory()
		file('output/libs').assertHasDescendants(sharedLibraryName('main/shared/jni-greeter'), 'jni-greeter.jar')
		jar("output/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
    }

    def "generate JNI headers when compiling Java source code"() {
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        file('build/generated/jni-headers').assertDoesNotExist()
        succeeds(taskNames.java.tasks.compile)

        then:
        file('build/generated/jni-headers').assertHasDescendants('com_example_greeter_Greeter.h')
    }

//    def "can compile and link against a library"() {
// TODO: JNI Plugin should allow to work alone by embedding the runtime native dependencies into a JAR. It should also create an empty JAR when no language (not even Java). This Jar should be exposed via API outgoing dependencies.

    protected TestFile sharedLibrary(String path) {
        return file(path)
    }

    protected TestFile executable(String path) {
        return file(path)
    }

    protected JarTestFixture jar(String path) {
        return new JarTestFixture(file(path))
    }

    // TODO: Should be abstracted
    protected List<String> getTasksToAssembleDevelopmentBinary() {
        // We are going to ignore the C++ compile task as the software model avoid creating the task when no sources
        return taskNames.cpp.tasks.allToSharedLibrary + taskNames.java.tasks.allToJar - [taskNames.cpp.tasks.compile]
    }

    protected String getDevelopmentBinaryNativeCompileTask() {
        return taskNames.cpp.tasks.compile
    }

    protected String getDevelopmentBinaryJvmCompileTask() {
        return taskNames.java.tasks.compile
    }

    protected void makeSingleProject() {
        buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
		settingsFile << "rootProject.name = 'jni-greeter'"
    }

    JavaJniCppGreeterLib getComponentUnderTest() {
        return new JavaJniCppGreeterLib('jni-greeter')
    }

	protected static String configurePlugins(List<String> pluginIds) {
		return """
			plugins {
				${pluginIds.collect { "id '$it'"}.join('\n')}
			}
		"""
	}
}
