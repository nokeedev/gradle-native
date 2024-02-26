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
	@SourceFileLocation(file = "jni-cpp-greeter/src/main/cpp/greeter.cpp")
	public SourceFile getSourceFile() {
		return sourceFile("cpp", "greeter.cpp", fromResource("jni-cpp-greeter/greeter.cpp").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
	}

	@Override
	@SourceFileLocation(file = "java-jni-greeter/src/main/headers/com_example_greeter_Greeter.h")
	public SourceFile getJniGeneratedHeaderFile() {
		return sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource("java-jni-greeter/com_example_greeter_Greeter.h").replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
	}
}
