package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

import static org.hamcrest.CoreMatchers.containsString

class JavaCppJniLibraryFunctionalTest extends AbstractFunctionalSpec implements MixedLanguageTaskNames {
	def "can apply plugins in what ever order"(pluginIds) {
		given:
		settingsFile << "rootProject.name = 'library'"
		buildFile << configurePlugins(pluginIds)

		when:
		succeeds('assemble')

		then:
		jar('build/libs/library.jar')
		jar('build/libs/library-macos-x86-64.jar')

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

    def "skip compilation and linking tasks when no source"() {
        given:
        makeSingleProject()

        expect:
        succeeds 'assemble'
        result.assertTasksExecuted(tasksToAssembleDevelopmentBinary, ':assemble')
        // TODO - should skip the task as NO-SOURCE
        result.assertTasksSkipped((tasksToAssembleDevelopmentBinary - [taskNames.java.tasks.jar, ':jniJar']))
        result.assertTasksNotSkipped(taskNames.java.tasks.jar, ':jniJar', ':assemble')
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

    // TODO: Maybe not
    def "adds shared library to JNI jar"() {
        makeSingleProject()
        settingsFile << "rootProject.name = 'jni-greeter'"
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':jniJar', ':assemble')
        result.assertTasksSkipped(taskNames.java.tasks.processResources)

        sharedLibrary("build/libs/main/shared/libjni-greeter.dylib").assertExists()
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class')
        jar("build/libs/jni-greeter-macos-x86-64.jar").hasDescendants('libjni-greeter.dylib')
    }

    def "build logic can change buildDir"() {
        makeSingleProject()
        settingsFile << "rootProject.name = 'jni-greeter'"
        componentUnderTest.writeToProject(testDirectory)

        given:
        buildFile << '''
            buildDir = 'output'
         '''

        expect:
        succeeds 'assemble'
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':jniJar', ':assemble')

        !file('build').exists()
        file('output/objs/main/shared/mainCpp').assertIsDirectory()
        sharedLibrary("output/libs/main/shared/libjni-greeter.dylib").assertExists()
		jar("output/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class')
        jar("output/libs/jni-greeter-macos-x86-64.jar").hasDescendants('libjni-greeter.dylib')
    }

    def "generate JNI headers when compiling Java source code"() {
        makeSingleProject()
        settingsFile << "rootProject.name = 'jni-greeter'"
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
        return taskNames.cpp.tasks.allToSharedLibrary + taskNames.java.tasks.allToJar + [':jniJar'] - [taskNames.cpp.tasks.compile]
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
