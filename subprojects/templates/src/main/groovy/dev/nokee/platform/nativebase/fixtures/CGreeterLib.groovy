package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile

class CGreeterLib extends GreeterImplementationAwareSourceElement<CGreeter> {
	@Delegate
	final NativeLibraryElement delegate

	CGreeterLib() {
		super(new CGreetUsingGreeter().asLib(), new CGreeter().asLib())
		delegate = ofNativeLibraryElements(elementUsingGreeter, greeter)
	}

	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter))
	}

	SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CGreeterTest())
	}

	private static class CGreetUsingGreeter extends NativeLibraryElement {
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

		CGreetUsingGreeter() {
			header = ofFile(sourceFile('headers', 'greet_alice.h', """
#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

EXPORT_FUNC void say_hello_to_alice();
"""))

			source = ofFiles(sourceFile('c', 'greet_alice.c', '''
#include "greet_alice.h"

#include <stdio.h>
#include "greeter.h"

void say_hello_to_alice() {
	printf("%s\\n", say_hello("Alice"));
}
'''))
		}
	}
}
