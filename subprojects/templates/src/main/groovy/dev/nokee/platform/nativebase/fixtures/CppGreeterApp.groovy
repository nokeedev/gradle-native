package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.test.fixtures.sources.NativeSourceElement.ofNativeElements

class CppGreeterApp extends GreeterImplementationAwareSourceElement<CppGreeter> {
	@Delegate final NativeSourceElement delegate

	CppGreeterApp() {
		super(new CppMainUsesGreeter(), new CppGreeter())
		delegate = ofNativeElements(elementUsingGreeter, greeter)
	}

	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter.asLib()))
	}
}

class CppMainUsesGreeter extends CppSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('cpp', 'main.cpp', '''
#include <iostream>
#include "greeter.h"

int main(int argc, char* argv[]) {
	std::cout << say_hello("Alice") << std::endl;
	return 0;
}
'''))
	}
}
