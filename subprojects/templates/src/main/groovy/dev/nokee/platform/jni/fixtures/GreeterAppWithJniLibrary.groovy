package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter

class GreeterAppWithJniLibrary implements ApplicationWithLibraryElement {
    final SourceElement library = new JavaJniCppGreeterLib()
    final SourceElement application = new JavaMainUsesGreeter()

    @Override
    String getExpectedOutput() {
        return application.expectedOutput
    }
}
