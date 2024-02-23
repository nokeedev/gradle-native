package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaNativeGreeter extends SourceFileElement {
	private final SourceFile source;
	private final JavaPackage javaPackage;
	private final String sharedLibraryBaseName;
	private final String resourcePath;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	public JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(javaPackage, sharedLibraryBaseName, "");
	}

	public JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this.javaPackage = javaPackage;
		this.sharedLibraryBaseName = sharedLibraryBaseName;
		this.resourcePath = resourcePath;
		source = sourceFile("java/" + javaPackage.getDirectoryLayout(), "Greeter.java", fromResource("java-jni-greeter/Greeter.java").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()).replace("${resourcePath}${sharedLibraryBaseName}", resourcePath + sharedLibraryBaseName));
	}

	public JavaNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
	}

	public JavaNativeGreeter withResourcePath(String resourcePath) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
	}
}
