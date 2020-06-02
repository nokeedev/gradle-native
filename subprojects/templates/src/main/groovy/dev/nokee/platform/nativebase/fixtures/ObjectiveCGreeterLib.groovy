package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter

class ObjectiveCGreeterLib extends ObjectiveCSourceElement {
	@Override
	SourceElement getSources() {
		return new ObjectiveCGreeter()
	}
}

