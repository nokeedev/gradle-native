package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement

class CppGreeterTest extends NativeSourceElement {

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
		return ofFiles(sourceFile('cpp', 'greeter_test.cpp', '''
			#include "greeter_fixtures.h"
			#include "greeter.h"

			int main(int argc, char* argv[]) {
				if (say_hello("Alice") == "Bonjour, Alice!") {
					return PASS;
				}
				return FAIL;
			}
		'''))
	}
}
