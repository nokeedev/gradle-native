package dev.nokee.platform.jni

import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class JavaCJniLibraryWithJUnitTestingFunctionalTest extends AbstractJavaJniLibraryWithJUnitTestingFunctionalTest {
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.c-language'
			}
		'''
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}
}
