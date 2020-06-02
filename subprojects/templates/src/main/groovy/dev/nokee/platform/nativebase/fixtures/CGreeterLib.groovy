package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.c.CSourceElement
import dev.nokee.platform.jni.fixtures.CGreeter

class CGreeterLib extends CSourceElement {
	@Override
	SourceElement getSources() {
		return new CGreeter()
	}
}
