package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaGreeterJUnitTest extends SourceFileElement {
	private final SourceFile source;

	@Override
	@SourceFileLocation(file = "java-jni-greeter/src/test/java/com/example/greeter/GreeterTest.java")
	public SourceFile getSourceFile() {
		return source;
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	public JavaGreeterJUnitTest() {
		this(ofPackage("com.example.greeter"));
	}

	public JavaGreeterJUnitTest(JavaPackage javaPackage) {
		source = sourceFile("java/" + javaPackage.getDirectoryLayout(), "GreeterTest.java", fromResource("java-jni-greeter/GreeterTest.java").replace("package com.example.greeter", "package " + javaPackage.getName()));
	}
}
