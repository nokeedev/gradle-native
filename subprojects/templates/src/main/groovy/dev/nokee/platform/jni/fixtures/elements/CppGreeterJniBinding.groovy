package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage

class CppGreeterJniBinding extends NativeSourceElement {
	private final source
	private final generatedHeader
	private final JavaPackage javaPackage

	@Override
	SourceElement getHeaders() {
		return empty()
	}

	@Override
	SourceElement getSources() {
		return source
	}

	CppGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage
		source = ofFiles(sourceFile('cpp', 'greeter.cpp', """
#include "${javaPackage.jniHeader('Greeter')}"

#include <cstring>
#include <cstdlib>

#include "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('Greeter', 'sayHello')}(JNIEnv * env, jobject self, jstring name_from_java) {
	// Ensure parameter isn't null
	if (name_from_java == nullptr) {
		return env->NewStringUTF(ERROR_STRING);
	}

	// Convert jstring to std::string
	const char *name_as_c_str = env->GetStringUTFChars(name_from_java, nullptr);
	auto result_from_cpp = say_hello(name_as_c_str);  // Call native library
	env->ReleaseStringUTFChars(name_from_java, name_as_c_str);

	// Convert std::string to jstring
	auto result_buffer = static_cast<char*>(std::malloc(result_from_cpp.size()));
	std::strcpy(result_buffer, result_from_cpp.c_str());
	auto result = env->NewStringUTF(result_buffer);

	// Return result back to Java
	return result;
}
"""))
		generatedHeader = ofFiles(sourceFile('headers', javaPackage.jniHeader('Greeter'), """
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_greeter_Greeter */

#ifndef _Included_com_example_greeter_Greeter
#define _Included_com_example_greeter_Greeter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:	 com_example_greeter_Greeter
 * Method:	sayHello
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('Greeter', 'sayHello')}
  (JNIEnv *, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif
"""))
	}

	NativeSourceElement withJniGeneratedHeader() {
		return new NativeSourceElement() {
			@Override
			SourceElement getHeaders() {
				return generatedHeader
			}

			@Override
			SourceElement getSources() {
				return CppGreeterJniBinding.this.source
			}
		}
	}
}
