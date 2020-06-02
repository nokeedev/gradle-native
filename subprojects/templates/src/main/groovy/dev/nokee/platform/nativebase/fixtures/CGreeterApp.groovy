package dev.nokee.platform.nativebase.fixtures


import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.c.CSourceElement
import dev.nokee.platform.jni.fixtures.CGreeter

class CGreeterApp extends CSourceElement {
	@Override
	SourceElement getSources() {
		return ofElements(new CMainUsesGreeter(), new CGreeter())
	}
}

class CMainUsesGreeter extends CSourceElement {
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
