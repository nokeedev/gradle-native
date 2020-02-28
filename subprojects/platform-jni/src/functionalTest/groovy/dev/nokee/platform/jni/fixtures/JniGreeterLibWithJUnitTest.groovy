package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest

class JniGreeterLibWithJUnitTest extends SourceElement {
    private final main = new JniGreeterLib()
    private final test = new JavaGreeterJUnitTest()

    @Override
    List<SourceFile> getFiles() {
        return main.files + test.files
    }

    @Override
    void writeToProject(TestFile projectDir) {
        main.writeToProject(projectDir)
        test.writeToProject(projectDir)
    }
}
