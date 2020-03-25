package dev.nokee.platform.jni

import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class JavaObjectiveCppJniLibraryWithJUnitTestingFunctionalTest extends AbstractJavaJniLibraryWithJUnitTestingFunctionalTest {
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			// Internal details
			tasks.withType(LinkSharedLibrary) {
				linkerArgs.add('-lobjc')
			}
		'''
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter')
	}
}
