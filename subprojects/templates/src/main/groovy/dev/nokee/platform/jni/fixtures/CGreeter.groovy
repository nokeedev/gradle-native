package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFileElement

class CGreeter extends NativeLibraryElement {
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

	CGreeter() {
		header = SourceFileElement.ofFile(sourceFile('headers', 'greeter.h', """
#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

#include <string.h>

EXPORT_FUNC char * say_hello(const char * name);
"""))
		source = SourceFileElement.ofFile(sourceFile('c', 'greeter_impl.c', """
#include "greeter.h"

#include <stdlib.h>
#include <string.h>

char * say_hello(const char * name) {
	static const char HELLO_STRING[] = "Bonjour, ";
	static const char PONCTUATION_STRING[] = "!";
	char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
	// TODO: Check for error code from malloc
	// TODO: Initialize result buffer to zeros
	strcpy(result, HELLO_STRING);
	strcat(result, name);
	strcat(result, PONCTUATION_STRING);
	return result;
}
"""))
	}

	NativeLibraryElement withOptionalFeature() {
		return new NativeLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return SourceFileElement.ofFile(sourceFile('c', 'greeter_impl.c', """
#include "greeter.h"

#include <stdlib.h>
#include <string.h>

char * say_hello(const char * name) {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	static const char HELLO_STRING[] = "Hello, ";
#else
	static const char HELLO_STRING[] = "Bonjour, ";
#endif
	static const char PONCTUATION_STRING[] = "!";
	char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
	// TODO: Check for error code from malloc
	// TODO: Initialize result buffer to zeros
	strcpy(result, HELLO_STRING);
	strcat(result, name);
	strcat(result, PONCTUATION_STRING);
	return result;
}
"""))
			}
		}
	}
}
