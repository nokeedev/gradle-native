package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.JniBindingElement;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public class CppGreeterJniBinding extends JniBindingElement {
	private final JavaPackage javaPackage;

	public CppGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage;
	}

	@Override
	public SourceFile getSourceFile() {
		return sourceFile("cpp", "greeter.cpp", fromResource(Source.class).replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
	}

	@SourceFileLocation(file = "jni-cpp-greeter/src/main/cpp/greeter.cpp")
	interface Source {}

	@Override
	public SourceFile getJniGeneratedHeaderFile() {
		return sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource(GreeterJniHeader.class).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
	}
}
