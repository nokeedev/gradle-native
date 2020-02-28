package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceElement
import dev.gradleplugins.test.fixtures.sources.cpp.CppSourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JniGreeterLib extends JniLibraryElement {
    private final SourceElement nativeJniSource
    private final SourceElement nativeImplSource
    private final SourceElement jvmSource

    @Override
    SourceElement getJvmSources() {
        return jvmSource
    }

    @Override
    SourceElement getNativeSources() {
        return ofElements(nativeJniSource, nativeImplSource);
    }

    JniGreeterLib() {
        def javaPackage = ofPackage('com.example.greeter')
        String sharedLibraryBaseName = 'jni-greeter'
        jvmSource = new JavaSourceFileElement() {
            final SourceFileElement source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'Greeter.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {

    static {
        try {
            System.loadLibrary("${sharedLibraryBaseName}");
        } catch (UnsatisfiedLinkError ex) {
            String libName = "${sharedLibraryBaseName}";
            URL url = Greeter.class.getClassLoader().getResource(libFilename(libName));
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

    public native String sayHello(String name);
}
"""))
        }

        nativeJniSource = new CppSourceElement() {
            final SourceElement headers = empty()
            final SourceElement sources = ofFiles(sourceFile('cpp', 'greeter.cpp', """
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
        }

        nativeImplSource = new CppSourceFileElement() {
            final SourceFileElement header = ofFile(sourceFile('headers', 'greeter.h', """
#pragma once

#include <string>

std::string say_hello(std::string name);
"""))
            final SourceFileElement source = ofFile(sourceFile('cpp', 'greeter_impl.cpp', """
#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
    return "Bonjour, " + name + "!";
}
"""))
        }
    }
}
