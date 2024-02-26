package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaNativeLoader extends SourceFileElement {
	private final SourceFile source;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "java-jni-greeter/src/main/java/com/example/greeter/NativeLoader.java")
	interface Content {}

	public JavaNativeLoader() {
		this(ofPackage("com.example.greeter"));
	}

	public JavaNativeLoader(JavaPackage javaPackage) {
		source = sourceFile("java/" + javaPackage.getDirectoryLayout(), "NativeLoader.java", fromResource(Content.class).replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()));
	}
}
