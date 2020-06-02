package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter

class CppGreeterApp extends CppSourceElement{
	@Override
	SourceElement getSources() {
		return ofElements(new CppMainUsesGreeter(), new CppGreeter())
	}
}

class CppMainUsesGreeter extends CppSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('cpp', 'main.cpp', '''
#include <iostream>
#include "greeter.h"

int main(int argc, char* argv[]) {
	std::cout << say_hello("Alice") << std::endl;
	return 0;
}
'''))
	}
}
