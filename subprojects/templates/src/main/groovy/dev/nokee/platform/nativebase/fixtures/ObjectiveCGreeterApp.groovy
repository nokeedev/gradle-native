package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements

class ObjectiveCGreeterApp extends GreeterImplementationAwareSourceElement<ObjectiveCGreeter> {
	@Delegate
	final NativeSourceElement delegate

	ObjectiveCGreeterApp() {
		super(new ObjectiveCMainUsesGreeter(), new ObjectiveCGreeter())
		delegate = ofNativeElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter.asLib()))
	}

	private static class ObjectiveCMainUsesGreeter extends NativeSourceElement {
		@Override
		SourceElement getSources() {
			return ofFiles(sourceFile('objc', 'main.m', '''
#include <stdio.h>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter new];
	printf("%s\\n", [greeter sayHello:"Alice"]);
	return 0;
}
'''))
		}
	}
}
