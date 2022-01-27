package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage
import dev.nokee.platform.jni.fixtures.elements.*

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage

class JavaJniObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	final ObjectiveCGreeterJniBinding nativeBindings
	final SourceElement jvmBindings
	final SourceElement jvmImplementation
	final ObjectiveCGreeter nativeImplementation

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	JavaJniObjectiveCGreeterLib(String projectName) {
		this(ofPackage('com.example.greeter'), projectName)
	}

	private JavaJniObjectiveCGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName), new ObjectiveCGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new ObjectiveCGreeter())
	}

	private JavaJniObjectiveCGreeterLib(JavaNativeGreeter jvmBindings, ObjectiveCGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, ObjectiveCGreeter nativeImplementation) {
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation)
		this.jvmBindings = jvmBindings
		this.nativeBindings = nativeBindings
		this.jvmImplementation = jvmImplementation
		this.nativeImplementation = nativeImplementation
	}

	JniLibraryElement withoutNativeImplementation() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation), nativeBindings)
	}

	JniLibraryElement withFoundationFrameworkDependency() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation, newJUnitTestElement()), ofNativeElements(nativeBindings, nativeImplementation.withFoundationFrameworkImplementation()))
	}

	JniLibraryElement withOptionalFeature() {
		return new SimpleJniLibraryElement(jvmSources, ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature()))
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, nativeImplementation.asLib()))
	}

	private static class ObjectiveCGreeterJniBinding extends NativeSourceElement {
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

		ObjectiveCGreeterJniBinding(JavaPackage javaPackage) {
			this.javaPackage = javaPackage
			source = ofFiles(sourceFile('objc', 'greeter.m', """
#include "${javaPackage.jniHeader('Greeter')}"

#include <string.h>
#include <stdlib.h>

#import "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('Greeter', 'sayHello')}(JNIEnv * env, jobject self, jstring name_from_java) {
	// Ensure parameter isn't null
	if (name_from_java == NULL) {
		return (*env)->NewStringUTF(env, ERROR_STRING);
	}

	// Convert jstring to std::string
	const char *name_as_c_str = (*env)->GetStringUTFChars(env, name_from_java, NULL);
    Greeter * greeter = [Greeter new];
    char * result_from_c = [greeter sayHello:name_as_c_str];  // Call native library
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
			return new NativeSourceElement() {
				@Override
				SourceElement getHeaders() {
					return generatedHeader
				}

				@Override
				SourceElement getSources() {
					return ObjectiveCGreeterJniBinding.this.source
				}
			}
		}
	}
}
