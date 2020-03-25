package dev.nokee.platform.jni

import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class JavaObjectiveCJniLibraryWithJUnitTestingFunctionalTest extends AbstractJavaJniLibraryWithJUnitTestingFunctionalTest {
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			// Internal details
			tasks.withType(LinkSharedLibrary) {
				linkerArgs.add('-lobjc')
			}
		'''
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter')
	}
}
