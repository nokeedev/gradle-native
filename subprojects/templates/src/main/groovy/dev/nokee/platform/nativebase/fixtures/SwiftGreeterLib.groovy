package dev.nokee.platform.nativebase.fixtures


import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter

class SwiftGreeterLib extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [new SwiftMainUsesGreeter().sourceFile, new SwiftGreeter().sourceFile]
	}
}

