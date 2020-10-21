package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement

class CppGreeter extends NativeLibraryElement {
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

	CppGreeter() {
		header = ofFiles(sourceFile('headers', 'greeter.h', """
#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

#include <string>

std::string EXPORT_FUNC say_hello(std::string name);
"""))
		source = ofFiles(sourceFile('cpp', 'greeter_impl.cpp', """
#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
	return "Bonjour, " + name + "!";
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
				return ofFiles(sourceFile('cpp', 'greeter_impl.cpp', """
#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	return "Hello, " + name + "!";
#else
	return "Bonjour, " + name + "!";
#endif
}
"""))
			}
		}
	}
}
