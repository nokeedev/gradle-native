package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivecpp.ObjectiveCppSourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter

class ObjectiveCppGreeterApp extends ObjectiveCppSourceElement {
	@Override
	SourceElement getSources() {
		return ofElements(new ObjectiveCppMainUsesGreeter(), new ObjectiveCppGreeter())
	}
}

class ObjectiveCppMainUsesGreeter extends ObjectiveCppSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objcpp', 'main.mm', '''
#include <iostream>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter new];
	std::cout << [greeter sayHello] << std::endl;
	[greeter release];
	return 0;
}
'''))
	}
}
