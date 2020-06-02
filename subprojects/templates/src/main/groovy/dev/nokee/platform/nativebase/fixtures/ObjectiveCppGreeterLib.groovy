package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivecpp.ObjectiveCppSourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter

class ObjectiveCppGreeterLib extends ObjectiveCppSourceElement {
	@Override
	SourceElement getSources() {
		return new ObjectiveCppGreeter()
	}
}

