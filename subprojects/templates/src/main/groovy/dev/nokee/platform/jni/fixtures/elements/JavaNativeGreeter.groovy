import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class JavaNativeGreeter extends JavaSourceFileElement {
	private final SourceFileElement source
	private final JavaPackage javaPackage
	private final String sharedLibraryBaseName
	private final String resourcePath

	@Override
	SourceFileElement getSource() {
		return source
	}

	JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath = '') {
		this.javaPackage = javaPackage
		this.sharedLibraryBaseName = sharedLibraryBaseName
		this.resourcePath = resourcePath
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
        NativeLoader.loadLibrary(Greeter.class.getClassLoader(), "${resourcePath}${sharedLibraryBaseName}");
    }

    public native String sayHello(String name);
}
"""))
	}

	JavaNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}

	JavaNativeGreeter withResourcePath(String resourcePath) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}
}
