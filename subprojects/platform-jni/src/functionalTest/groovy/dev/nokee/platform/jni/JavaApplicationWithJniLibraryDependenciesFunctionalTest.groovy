package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.nokee.platform.jni.fixtures.GreeterAppWithJniLibrary
import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement

class JavaApplicationWithJniLibraryDependenciesFunctionalTest extends AbstractFunctionalSpec  {
    private void makeComponentWithLibrary() {
        settingsFile << '''
            rootProject.name = 'app'
            include 'jni-greeter'
        '''
        buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }
            
            application {
                mainClassName = 'com.example.app.Main'
            }
            
            dependencies {
                implementation project(':jni-greeter')
            }
        '''
        file('jni-greeter/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''

        componentsUnderTest.library.writeToProject(testDirectory.file('jni-greeter'))
        componentsUnderTest.application.writeToProject(testDirectory)
    }

    private void makeComponentWithIncludedBuildLibrary() {
        settingsFile << '''
            rootProject.name = 'app'
            includeBuild 'jni-greeter'
        '''
        buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }
            
            application {
                mainClassName = 'com.example.app.Main'
            }
            
            dependencies {
                implementation 'com.example.greeter:jni-greeter:1.0'
            }
        '''

        file('jni-greeter/settings.gradle') << "rootProject.name = 'jni-greeter'"
        file('jni-greeter/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
            
            group = 'com.example.greeter'
            version = '1.0'
        '''

        componentsUnderTest.library.writeToProject(testDirectory.file('jni-greeter'))
        componentsUnderTest.application.writeToProject(testDirectory)
    }

    private ApplicationWithLibraryElement getComponentsUnderTest() {
        return new GreeterAppWithJniLibrary()
    }

    def "can run application as multi-project dependency"() {
        makeComponentWithLibrary()

        when:
        succeeds('run')

        then:
        result.assertOutputContains(componentsUnderTest.expectedOutput)
    }

    def "can run application as included build dependency"() {
        makeComponentWithIncludedBuildLibrary()

        when:
        succeeds('run')

        then:
        result.assertOutputContains(componentsUnderTest.expectedOutput)
    }
}
