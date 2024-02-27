package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaGreeter extends SourceFileElement {
	private final SourceFile source;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "java-greeter/src/main/java/com/example/greeter/Greeter.java", properties = {
		@SourceFileProperty(regex = "^package\\s+(com.example.greeter);$", name = "package")
	})
	interface Source {}

	public JavaGreeter() {
		this(ofPackage("com.example.greeter"));
	}

	public JavaGreeter(JavaPackage javaPackage) {
		source = sourceFile("java/" + javaPackage.getDirectoryLayout(), "Greeter.java", fromResource(Source.class, it -> it.put("package", javaPackage.getName())));
	}
}
