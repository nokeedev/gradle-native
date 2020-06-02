package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter

class CppGreeterLib extends CppSourceElement{
	@Override
	SourceElement getSources() {
		return new CppGreeter()
	}
}

