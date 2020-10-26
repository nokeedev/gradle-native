package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements

class CGreeterApp extends GreeterImplementationAwareSourceElement<CGreeter> {
	@Delegate
	final NativeSourceElement delegate

	CGreeterApp() {
		super(new CMainUsesGreeter(), new CGreeter())
		delegate = ofNativeElements(elementUsingGreeter, greeter)
	}

	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectName) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectName, greeter.asLib()))
	}

	SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CGreeterTest())
	}

	private static class CMainUsesGreeter extends NativeSourceElement {
		@Override
		SourceElement getSources() {
			return ofFiles(sourceFile('c', 'main.c', '''
#include <stdio.h>
#include "greeter.h"

int main(int argc, char** argv) {
	printf("%s\\n", say_hello("Alice"));
	return 0;
}
'''))
		}
	}
}
