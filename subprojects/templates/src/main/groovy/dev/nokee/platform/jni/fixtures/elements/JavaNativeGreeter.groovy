package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage

class JavaNativeGreeter extends SourceFileElement {
	private final SourceFile source
	private final JavaPackage javaPackage
	private final String sharedLibraryBaseName
	private final String resourcePath

	@Override
	SourceFile getSourceFile() {
		return source
	}

	JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath = '') {
		this.javaPackage = javaPackage
		this.sharedLibraryBaseName = sharedLibraryBaseName
		this.resourcePath = resourcePath
		source = sourceFile("java/${javaPackage.directoryLayout}", 'Greeter.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {

    static {
        NativeLoader.loadLibrary(Greeter.class.getClassLoader(), "${resourcePath}${sharedLibraryBaseName}");
    }

    public native String sayHello(String name);
}
""")
	}

	JavaNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}

	JavaNativeGreeter withResourcePath(String resourcePath) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}
}
