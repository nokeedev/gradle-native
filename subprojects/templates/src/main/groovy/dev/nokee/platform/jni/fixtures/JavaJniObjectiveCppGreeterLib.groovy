package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.gradleplugins.test.fixtures.sources.objectivecpp.ObjectiveCppLibraryElement
import dev.gradleplugins.test.fixtures.sources.objectivecpp.ObjectiveCppSourceElement
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniObjectiveCppGreeterLib extends JniLibraryElement {
	final ObjectiveCppGreeterJniBinding nativeBindings
	final JavaSourceElement jvmBindings
	final JavaSourceElement jvmImplementation
	final ObjectiveCppGreeter nativeImplementation
	final JavaSourceElement junitTest

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	JavaJniObjectiveCppGreeterLib(String projectName) {
		def javaPackage = ofPackage('com.example.greeter')
		String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeGreeter(javaPackage, sharedLibraryBaseName)
		nativeBindings = new ObjectiveCppGreeterJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);

		nativeImplementation = new ObjectiveCppGreeter()

		junitTest = new JavaGreeterJUnitTest()
	}

	JniLibraryElement withoutNativeImplementation() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniObjectiveCppGreeterLib.this.jvmBindings, JavaJniObjectiveCppGreeterLib.this.jvmImplementation)
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
				return ofElements(JavaJniObjectiveCppGreeterLib.this.jvmBindings, JavaJniObjectiveCppGreeterLib.this.jvmImplementation, junitTest)
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
				return JavaJniObjectiveCppGreeterLib.this.jvmSources
			}

			@Override
			NativeSourceElement getNativeSources() {
				return ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature())
			}
		}
	}
}


class ObjectiveCppGreeter extends ObjectiveCppLibraryElement {
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

	ObjectiveCppGreeter() {
		header = ofFile(sourceFile('headers', 'greeter.h', """
#include <string>

@interface Greeter
+ (id)alloc;
- (std::string)sayHello:(std::string)name;
@end
"""))
		source = ofFile(sourceFile('objcpp', 'greeter_impl.mm', """
#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)alloc {
    return class_createInstance(self, 0);
}

- (std::string)sayHello:(std::string)name {
    return "Bonjour, " + name + "!";
}
@end
"""))
	}

	ObjectiveCppLibraryElement withOptionalFeature() {
		return new ObjectiveCppLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('objcpp', 'greeter_impl.mm', """
#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)alloc {
    return class_createInstance(self, 0);
}

- (std::string)sayHello:(std::string)name {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	return "Hello, " + name + "!";
#else
    return "Bonjour, " + name + "!";
#endif
}
@end
"""))
			}
		}
	}
}

class ObjectiveCppGreeterJniBinding extends ObjectiveCppSourceElement {
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

	ObjectiveCppGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage
		source = ofFiles(sourceFile('objcpp', 'greeter.mm', """
#include "${javaPackage.jniHeader('Greeter')}"

#include <string.h>
#include <stdlib.h>

#import "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('Greeter', 'sayHello')}(JNIEnv * env, jobject self, jstring name_from_java) {
    // Ensure parameter isn't null
    if (name_from_java == nullptr) {
        return env->NewStringUTF(ERROR_STRING);
    }

    // Convert jstring to std::string
    const char *name_as_c_str = env->GetStringUTFChars(name_from_java, nullptr);
    Greeter * greeter = [Greeter alloc];
    auto result_from_cpp = [greeter sayHello:name_as_c_str];  // Call native library
    env->ReleaseStringUTFChars(name_from_java, name_as_c_str);

    // Convert std::string to jstring
    jstring result = env->NewStringUTF(result_from_cpp.c_str());

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
		return new ObjectiveCppSourceElement() {
			@Override
			SourceElement getHeaders() {
				return generatedHeader
			}

			@Override
			SourceElement getSources() {
				return ObjectiveCppGreeterJniBinding.this.source
			}
		}
	}
}
