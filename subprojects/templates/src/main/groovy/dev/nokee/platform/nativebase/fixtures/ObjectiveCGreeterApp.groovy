package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter

class ObjectiveCGreeterApp extends ObjectiveCSourceElement {
	@Override
	SourceElement getSources() {
		return ofElements(new ObjectiveCMainUsesGreeter(), new ObjectiveCGreeter())
	}
}

class ObjectiveCMainUsesGreeter extends ObjectiveCSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objc', 'main.m', '''
#include <stdio.h>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter alloc];
	printf("%s\\n", [greeter sayHello:"Alice"]);
	return 0;
}
'''))
	}
}
