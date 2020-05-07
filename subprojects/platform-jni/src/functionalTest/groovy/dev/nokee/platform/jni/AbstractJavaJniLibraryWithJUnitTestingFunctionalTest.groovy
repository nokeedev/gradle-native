package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

abstract class AbstractJavaJniLibraryWithJUnitTestingFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can test library using JUnit"() {
		makeSingleProject()
		buildFile << '''
			repositories {
				mavenCentral()
			}

			dependencies {
				testImplementation 'junit:junit:4.12'
			}
		'''
		componentUnderTest.writeToProject(testDirectory)
		new JavaGreeterJUnitTest().writeToProject(testDirectory)

		expect:
		succeeds('test')
		// TODO: assert JUnit result
	}

	protected abstract void makeSingleProject()

	protected abstract JniLibraryElement getComponentUnderTest()
}
