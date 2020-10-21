package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement

class CGreeterTest extends NativeSourceElement {

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
		return ofFiles(sourceFile('c', 'greeter_test.c', '''
			#include "greeter_fixtures.h"
			#include <string.h>
			#include "greeter.h"

			int main(int argc, char** argv) {
				char * value = say_hello("Alice");
				if (0 == strcmp(value, "Bonjour, Alice!")) {
					free(value);
					return PASS;
				}
				free(value);
				return FAIL;
			}
		'''))
	}
}
