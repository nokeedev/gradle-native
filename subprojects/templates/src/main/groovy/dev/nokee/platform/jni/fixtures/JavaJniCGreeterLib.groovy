package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.c.CLibraryElement
import dev.gradleplugins.test.fixtures.sources.c.CSourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniCGreeterLib extends JniLibraryElement {
	final CGreeterJniBinding nativeBindings
	final JavaSourceElement jvmBindings
	final JavaSourceElement jvmImplementation
	final CGreeter nativeImplementation
	final JavaSourceElement junitTest

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	JavaJniCGreeterLib(String projectName) {
		def javaPackage = ofPackage('com.example.greeter')
		String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeGreeter(javaPackage, sharedLibraryBaseName)
		nativeBindings = new CGreeterJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);

		nativeImplementation = new CGreeter()

		junitTest = new JavaGreeterJUnitTest()
	}

	JniLibraryElement withoutNativeImplementation() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniCGreeterLib.this.jvmBindings, JavaJniCGreeterLib.this.jvmImplementation)
			}

			@Override
			NativeSourceElement getNativeSources() {
				return nativeBindings
			}
		}
	}

	JniLibraryElement withJUnitTest() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniCGreeterLib.this.jvmBindings, JavaJniCGreeterLib.this.jvmImplementation, junitTest)
			}

			@Override
			NativeSourceElement getNativeSources() {
				return ofNativeElements(nativeBindings, nativeImplementation)
			}
		}
	}

	JniLibraryElement withOptionalFeature() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return JavaJniCGreeterLib.this.jvmSources
			}

			@Override
			NativeSourceElement getNativeSources() {
				return ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature())
			}
		}
	}
}


class CGreeter extends CLibraryElement {
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
		header = ofFile(sourceFile('headers', 'greeter.h', """
#pragma once

#include <string.h>

char * say_hello(const char * name);
"""))
		source = ofFile(sourceFile('c', 'greeter_impl.c', """
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

	CLibraryElement withOptionalFeature() {
		return new CLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('c', 'greeter_impl.c', """
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

class CGreeterJniBinding extends CSourceElement {
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

	CGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage
		source = ofFiles(sourceFile('c', 'greeter.c', """
#include "${javaPackage.jniHeader('Greeter')}"

#include <string.h>
#include <stdlib.h>

#include "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('Greeter', 'sayHello')}(JNIEnv * env, jobject self, jstring name_from_java) {
	// Ensure parameter isn't null
	if (name_from_java == NULL) {
		return (*env)->NewStringUTF(env, ERROR_STRING);
	}

	// Convert jstring to std::string
	const char *name_as_c_str = (*env)->GetStringUTFChars(env, name_from_java, NULL);
	char * result_from_c = say_hello(name_as_c_str);  // Call native library
	(*env)->ReleaseStringUTFChars(env, name_from_java, name_as_c_str);

	// Convert std::string to jstring
	jstring result = (*env)->NewStringUTF(env, result_from_c);

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
 * Class:     com_example_greeter_Greeter
 * Method:    sayHello
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

	SourceElement withJniGeneratedHeader() {
		return new CppSourceElement() {
			@Override
			SourceElement getHeaders() {
				return generatedHeader
			}

			@Override
			SourceElement getSources() {
				return CGreeterJniBinding.this.source
			}
		}
	}
}
