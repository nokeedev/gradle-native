package dev.nokee.platform.jni.fixtures


import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppLibraryElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniCppGreeterLib extends JniLibraryElement {
    final CppGreeterJniBinding nativeBindings
    final JavaSourceElement jvmBindings
	final JavaSourceElement jvmImplementation
    final CppLibraryElement nativeImplementation
	final JavaSourceElement junitTest

    @Override
    SourceElement getJvmSources() {
        return ofElements(jvmBindings, jvmImplementation)
    }

    @Override
    SourceElement getNativeSources() {
        return ofElements(nativeBindings, nativeImplementation);
    }

	JavaJniCppGreeterLib(String projectName) {
        def javaPackage = ofPackage('com.example.greeter')
        String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeGreeter(javaPackage, sharedLibraryBaseName)
        nativeBindings = new CppGreeterJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);

        nativeImplementation = new CppGreeter()

		junitTest = new JavaGreeterJUnitTest()
    }

    JniLibraryElement withoutNativeImplementation() {
        return new JniLibraryElement() {
            @Override
            SourceElement getJvmSources() {
                return ofElements(JavaJniCppGreeterLib.this.jvmBindings, JavaJniCppGreeterLib.this.jvmImplementation)
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
				return ofElements(JavaJniCppGreeterLib.this.jvmBindings, JavaJniCppGreeterLib.this.jvmImplementation, junitTest)
			}

			@Override
			SourceElement getNativeSources() {
				return ofElements(nativeBindings, nativeImplementation)
			}
		}
	}
}

class JavaNativeLoader extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	JavaNativeLoader(JavaPackage javaPackage) {
		source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'NativeLoader.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class NativeLoader {

    public static void loadLibrary(ClassLoader classLoader, String libName) {
		try {
            System.loadLibrary(libName);
        } catch (UnsatisfiedLinkError ex) {
            URL url = classLoader.getResource(libFilename(libName));
            try {
                File file = Files.createTempFile("jni", "greeter").toFile();
                file.deleteOnExit();
                file.delete();
                try (InputStream in = url.openStream()) {
                    Files.copy(in, file.toPath());
                }
                System.load(file.getCanonicalPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
	}

    private static String libFilename(String libName) {
        // TODO depend on OS
        return "lib" + libName + ".dylib";
    }
}
"""))
	}
}

class JavaNativeGreeter extends JavaSourceFileElement {
    private final SourceFileElement source

    @Override
    SourceFileElement getSource() {
        return source
    }

    JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName) {
        source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'Greeter.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {

    static {
        NativeLoader.loadLibrary(Greeter.class.getClassLoader(), "${sharedLibraryBaseName}");
    }

    public native String sayHello(String name);
}
"""))
    }
}

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

#include <string>

std::string say_hello(std::string name);
"""))
        source = ofFile(sourceFile('cpp', 'greeter_impl.cpp', """
#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
    return "Bonjour, " + name + "!";
}
"""))
    }
}

class CppGreeterJniBinding extends CppSourceElement {
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
				return CppGreeterJniBinding.this.source
			}
		}
	}
}

class JavaGreeterJUnitTest extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	@Override
	String getSourceSetName() {
		return 'test'
	}

	JavaGreeterJUnitTest() {
		source = ofFile(sourceFile('java/com/example/greeter', 'GreeterTest.java', '''
package com.example.greeter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GreeterTest {
    @Test
    public void testGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello("World");
        assertThat(greeting, equalTo("Bonjour, World!"));
    }

    @Test
    public void testNullGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello(null);
        assertThat(greeting, equalTo("name cannot be null"));
    }
}
'''))
	}
}
