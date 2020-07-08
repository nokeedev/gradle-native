package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.test.fixtures.sources.NativeSourceElement.ofNativeElements

class ObjectiveCGreeterApp extends GreeterImplementationAwareSourceElement<ObjectiveCGreeter> {
	@Delegate final NativeSourceElement delegate

	ObjectiveCGreeterApp() {
		super(new ObjectiveCMainUsesGreeter(), new ObjectiveCGreeter())
		delegate = ofNativeElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter.asLib()))
	}
}

class ObjectiveCMainUsesGreeter extends ObjectiveCSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objc', 'main.m', '''
#include <stdio.h>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter alloc];
	printf("%s\\n", [greeter sayHello:"Alice"]);
	return 0;
}
'''))
	}
}
