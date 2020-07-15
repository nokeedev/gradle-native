package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement

class CppGreeterTest extends NativeSourceElement {

	@Override
	String getSourceSetName() {
		return 'test'
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('cpp', 'greeter_test.cpp', '''
			#include "greeter.h"

			int main(int argc, char* argv[]) {
				if (say_hello("Alice") == "Bonjour, Alice!") {
					return 0;
				}
				return -1;
			}
		'''))
	}
}
