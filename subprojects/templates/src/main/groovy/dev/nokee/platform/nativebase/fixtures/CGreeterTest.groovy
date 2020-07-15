package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement

class CGreeterTest extends NativeSourceElement {

	@Override
	String getSourceSetName() {
		return 'test'
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('c', 'greeter_test.c', '''
			#include <string.h>
			#include "greeter.h"

			int main(int argc, char** argv) {
				char * value = say_hello("Alice");
				if (0 == strcmp(value, "Bonjour, Alice!")) {
					free(value);
					return 0;
				}
				free(value);
				return -1;
			}
		'''))
	}
}
