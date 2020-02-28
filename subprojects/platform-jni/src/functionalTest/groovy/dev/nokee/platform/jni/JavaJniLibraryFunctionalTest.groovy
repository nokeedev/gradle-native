package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JniGreeterLib

import static org.hamcrest.CoreMatchers.containsString

class JavaJniLibraryFunctionalTest extends AbstractFunctionalSpec implements MixedLanguageTaskNames {

    def "skip compilation and linking tasks when no source"() {
        given:
        makeSingleProject()

        expect:
        succeeds 'assemble'
        result.assertTasksExecuted(tasksToAssembleDevelopmentBinary, ':assemble')
        // TODO - should skip the task as NO-SOURCE
        result.assertTasksSkipped((tasksToAssembleDevelopmentBinary - [taskNames.java.tasks.jar]))
        result.assertTasksNotSkipped(taskNames.java.tasks.jar, ':assemble')
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
    def "adds shared library to jar"() {
        makeSingleProject()
        settingsFile << "rootProject.name = 'jni-greeter'"
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':assemble')
        result.assertTasksSkipped(taskNames.java.tasks.processResources)

        sharedLibrary("build/libs/main/shared/libjni-greeter.dylib").assertExists()
        jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'libjni-greeter.dylib')
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
        result.assertTasksExecuted(taskNames.cpp.tasks.allToSharedLibrary, taskNames.java.tasks.allToJar, ':assemble')

        !file('build').exists()
        file('output/objs/main/shared/mainCpp').assertIsDirectory()
        sharedLibrary("output/libs/main/shared/libjni-greeter.dylib").assertExists()
        jar("output/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'libjni-greeter.dylib')
    }

//    def "can compile and link against a library"() {


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
    }

    JniGreeterLib getComponentUnderTest() {
        return new JniGreeterLib()
    }
}
