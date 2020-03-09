package dev.nokee.platform.jni.fixtures


import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter

class GreeterAppWithJniLibrary implements ApplicationWithLibraryElement {
	final JavaJniCppGreeterLib library
	final JavaMainUsesGreeter application = new JavaMainUsesGreeter()

	GreeterAppWithJniLibrary(String projectName) {
		library = new JavaJniCppGreeterLib(projectName)
	}

	@Override
	String getExpectedOutput() {
		return application.expectedOutput
	}
}
