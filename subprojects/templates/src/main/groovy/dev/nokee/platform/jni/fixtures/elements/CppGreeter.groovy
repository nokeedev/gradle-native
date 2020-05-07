package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class CppGreeter extends CppLibraryElement {
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
		header = ofFile(sourceFile('headers', 'greeter.h', """
#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

#include <string>

std::string EXPORT_FUNC say_hello(std::string name);
"""))
		source = ofFile(sourceFile('cpp', 'greeter_impl.cpp', """
#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
	return "Bonjour, " + name + "!";
}
"""))
	}

	CppLibraryElement withOptionalFeature() {
		return new CppLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('cpp', 'greeter_impl.cpp', """
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
