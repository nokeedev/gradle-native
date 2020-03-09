package dev.nokee.platform.jni


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class JavaCppJniLibraryWithJUnitTestingFunctionalTest extends AbstractInstalledToolChainIntegrationSpec implements MixedLanguageTaskNames {
	def "can test library using JUnit"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('test')

		then:
		result.assertTasksExecuted(taskNames.java.tasks.allToTest, taskNames.cpp.tasks.allToLink)
		result.assertTasksSkipped(taskNames.java.tasks.main.processResources, taskNames.java.tasks.test.processResources)
		result.assertTasksNotSkipped(taskNames.java.tasks.allToTest - [taskNames.java.tasks.main.processResources, taskNames.java.tasks.test.processResources], taskNames.cpp.tasks.allToLink)
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

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter').withJUnitTest()
	}
}
