package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaMainUsesGreeter extends SourceFileElement implements GreeterElement {
	private final SourceFile main;

	@Override
	public SourceFile getSourceFile() {
		return main;
	}

    public JavaMainUsesGreeter() {
		JavaPackage javaPackage = ofPackage("com.example.app");
		main = sourceFile("java/" + javaPackage.getDirectoryLayout(), "Main.java", fromResource("java-jni-app/Main.java").replace("package " + ofPackage("com.example.app").getName(), "package " + javaPackage.getName()));
	}

	@Override
	public String getExpectedOutput() {
		return "Bonjour, World!";
	}
}
