package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCLibraryElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniObjectiveCGreeterLib extends JniLibraryElement {
	final ObjectiveCGreeterJniBinding nativeBindings
	final JavaSourceElement jvmBindings
	final JavaSourceElement jvmImplementation
	final ObjectiveCLibraryElement nativeImplementation
	final JavaSourceElement junitTest

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	SourceElement getNativeSources() {
		return ofElements(nativeBindings, nativeImplementation);
	}

	JavaJniObjectiveCGreeterLib(String projectName) {
		def javaPackage = ofPackage('com.example.greeter')
		String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeGreeter(javaPackage, sharedLibraryBaseName)
		nativeBindings = new ObjectiveCGreeterJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);

		nativeImplementation = new ObjectiveCGreeter()

		junitTest = new JavaGreeterJUnitTest()
	}

	JniLibraryElement withoutNativeImplementation() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniObjectiveCGreeterLib.this.jvmBindings, JavaJniObjectiveCGreeterLib.this.jvmImplementation)
			}

			@Override
			SourceElement getNativeSources() {
				return nativeBindings
			}
		}
	}

	JniLibraryElement withJUnitTest() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniObjectiveCGreeterLib.this.jvmBindings, JavaJniObjectiveCGreeterLib.this.jvmImplementation, junitTest)
			}

			@Override
			SourceElement getNativeSources() {
				return ofElements(nativeBindings, nativeImplementation)
			}
		}
	}

	JniLibraryElement withFoundationFrameworkDependency() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniObjectiveCGreeterLib.this.jvmBindings, JavaJniObjectiveCGreeterLib.this.jvmImplementation, junitTest)
			}

			@Override
			SourceElement getNativeSources() {
				return ofElements(nativeBindings, nativeImplementation.withFoundationFrameworkImplementation())
			}
		}
	}
}


class ObjectiveCGreeter extends ObjectiveCLibraryElement {
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

	ObjectiveCGreeter() {
		header = ofFile(sourceFile('headers', 'greeter.h', """
@interface Greeter
+ (id)alloc;
- (char *)sayHello:(const char *)name;
@end
"""))
		source = ofFile(sourceFile('objc', 'greeter_impl.m', """
#import "greeter.h"

#include <stdlib.h>
#include <string.h>
#include <objc/runtime.h>

@implementation Greeter

+(id)alloc {
    return class_createInstance(self, 0);
}

- (char *)sayHello:(const char *)name {
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
@end
"""))
	}

	ObjectiveCLibraryElement withFoundationFrameworkImplementation() {
		return new ObjectiveCLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('objc', 'greeter_impl.m', """
#import "greeter.h"

#include <stdlib.h>
#include <string.h>
#include <objc/runtime.h>
#include <Foundation/NSString.h>

@implementation Greeter

+(id)alloc {
    return class_createInstance(self, 0);
}

- (char *)sayHello:(const char *)name {
    NSString * result_nsstring = [NSString stringWithFormat:@"Bonjour, %s!", name];
    char *result = calloc([result_nsstring length]+1, 1);
	strncpy(result, [result_nsstring UTF8String], [result_nsstring length]);
    return result;
}
@end
"""))
			}
		}
	}
}

class ObjectiveCGreeterJniBinding extends ObjectiveCSourceElement {
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
    Greeter * greeter = [Greeter alloc];
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
		return new ObjectiveCSourceElement() {
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
