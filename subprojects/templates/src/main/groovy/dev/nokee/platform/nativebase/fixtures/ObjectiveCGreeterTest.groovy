package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCGreeterTest extends NativeSourceElement {
	@Override
	String getSourceSetName() {
		return 'test'
	}

	@Override
	SourceElement getHeaders() {
		return ofFiles(sourceFile('headers', 'greeter_fixtures.h', '''
			#define PASS 0
			#define FAIL -1
		'''))
	}

	@Override
	SourceElement getSources() {
		return ofFile(sourceFile('objc', 'greeter_test.m', '''
			#include <objc/runtime.h>

			#include "greeter_fixtures.h"
			#include "greeter.h"

			int main(int argc, const char ** argv) {
				Greeter* greeter = [Greeter alloc];
				if ([greeter sayHello:"Alice"] == "Bonjour, Alice!") {
					return PASS;
				}
				return FAIL;
			}
		'''))
	}
}
