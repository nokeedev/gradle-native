package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCppGreeterTest extends NativeSourceElement {
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
		return ofFile(sourceFile('objcpp', 'greeter_test.mm', '''
			#include <objc/runtime.h>

			#include "greeter_fixtures.h"
			#include "greeter.h"

			int main(int argc, const char * argv[]) {
				Greeter* greeter = [Greeter alloc];
				if ([greeter sayHello:"Alice"] == "Bonjour, Alice!") {
					return PASS;
				}
				return FAIL;
			}
		'''))
	}
}
