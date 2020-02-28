package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JniGreeterLibWithJUnitTest

class JavaJniLibraryWithJUnitTestingFunctionalTest extends AbstractFunctionalSpec implements MixedLanguageTaskNames {
    def "can test library using JUnit"() {
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('test')

        then:
        result.assertTasksExecuted(taskNames.java.tasks.allToTest, taskNames.cpp.tasks.allToSharedLibrary)
        result.assertTasksSkipped(taskNames.java.tasks.main.processResources, taskNames.java.tasks.test.processResources)
        result.assertTasksNotSkipped(taskNames.java.tasks.allToTest - [taskNames.java.tasks.main.processResources, taskNames.java.tasks.test.processResources], taskNames.cpp.tasks.allToSharedLibrary)
        // TODO: assert JUnit result
    }

    protected void makeSingleProject() {
        settingsFile << "rootProject.name = 'jni-greeter'"
        buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        '''
    }

    protected JniGreeterLibWithJUnitTest getComponentUnderTest() {
        return new JniGreeterLibWithJUnitTest()
    }
}
