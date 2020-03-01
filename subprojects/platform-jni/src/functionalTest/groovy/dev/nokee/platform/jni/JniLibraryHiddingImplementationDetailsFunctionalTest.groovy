package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class JniLibraryHiddingImplementationDetailsFunctionalTest extends AbstractFunctionalSpec {
    def "hides software model implementation details tasks"() {
        settingsFile << "rootProject.name = 'lib'"
        buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
        new JavaJniCppGreeterLib().writeToProject(testDirectory)

        when:
        succeeds('tasks')

        then:
        result.assertNotOutput("mainSharedLibrary - Assembles shared library 'main:sharedLibrary'.")
        result.assertNotOutput("mainStaticLibrary - Assembles static library 'main:staticLibrary'.")
        result.assertNotOutput("assembleDependentsMain - Assemble dependents of native library 'main'.")
        result.assertNotOutput("assembleDependentsMainSharedLibrary - Assemble dependents of shared library 'main:sharedLibrary'.")
        result.assertNotOutput("assembleDependentsMainStaticLibrary - Assemble dependents of static library 'main:staticLibrary'.")
        result.assertNotOutput("buildDependentsMain - Build dependents of native library 'main'.")
        result.assertNotOutput("buildDependentsMainSharedLibrary - Build dependents of shared library 'main:sharedLibrary'.")
        result.assertNotOutput("buildDependentsMainStaticLibrary - Build dependents of static library 'main:staticLibrary'.")
        result.assertNotOutput("components - Displays the components produced by root project 'lib'. [incubating]");
        result.assertNotOutput("dependentComponents - Displays the dependent components of components in root project 'lib'. [incubating]");
    }

    // TODO: Assert Java and native plugins are applied
}
