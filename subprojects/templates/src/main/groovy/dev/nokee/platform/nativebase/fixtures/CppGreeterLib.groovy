package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppLibraryElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.test.fixtures.sources.NativeSourceElement.ofNativeElements
import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class CppGreeterLib extends GreeterImplementationAwareSourceElement<CppGreeter> {
	@Delegate final NativeLibraryElement delegate

	CppGreeterLib() {
		super(new CppGreetUsingGreeter().asLib(), new CppGreeter().asLib())
		delegate = ofNativeLibraryElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter))
	}

	SourceElement withGenericTestSuite() {
		return ofNativeLibraryElements(delegate, new CppGreeterTest())
	}
}

class CppGreetUsingGreeter extends CppLibraryElement {
	private final header
	private final source

	@Override
	SourceElement getPublicHeaders() {
		return header
	}

	@Override
	SourceElement getSources() {
		return source
	}

	CppGreetUsingGreeter() {
		header = ofFile(sourceFile('headers', 'greet_alice.h', """
#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

EXPORT_FUNC void say_hello_to_alice();
"""))

		source = ofFiles(sourceFile('cpp', 'greet_alice.cpp', '''
#include "greet_alice.h"

#include <iostream>
#include "greeter.h"

void say_hello_to_alice() {
	std::cout << say_hello("Alice") << std::endl;
}
'''))
	}
}
