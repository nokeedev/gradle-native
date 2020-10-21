package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements

class ObjectiveCppGreeterApp extends GreeterImplementationAwareSourceElement<ObjectiveCppGreeter> {
	@Delegate
	final NativeSourceElement delegate

	ObjectiveCppGreeterApp() {
		super(new ObjectiveCppMainUsesGreeter(), new ObjectiveCppGreeter())
		delegate = ofNativeElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter.asLib()))
	}

	private static class ObjectiveCppMainUsesGreeter extends NativeSourceElement {
		@Override
		SourceElement getSources() {
			return ofFiles(sourceFile('objcpp', 'main.mm', '''
#include <iostream>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter alloc];
	std::cout << [greeter sayHello:"Alice"] << std::endl;
	return 0;
}
'''))
		}
	}
}
