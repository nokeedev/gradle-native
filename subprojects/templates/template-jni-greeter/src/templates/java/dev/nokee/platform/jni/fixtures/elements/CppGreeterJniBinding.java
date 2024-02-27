package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.JniBindingElement;

public final class CppGreeterJniBinding extends JniBindingElement {
	private final JavaPackage javaPackage;

	public CppGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage;
	}

	@Override
	public SourceFile getSourceFile() {
		return sourceFile("cpp", "greeter.cpp", fromResource(Source.class, it -> {
			it.put("jniHeader", javaPackage.jniHeader("Greeter"));
			it.put("methodName", javaPackage.jniMethodName("Greeter", "sayHello"));
		}));
	}

	@SourceFileLocation(file = "jni-cpp-greeter/src/main/cpp/greeter.cpp", properties = {
		@SourceFileProperty(regex = "^#include\\s+\"(com_example_greeter_Greeter.h)\"$", name = "jniHeader"),
		@SourceFileProperty(regex = "\\s+(Java_com_example_greeter_Greeter_sayHello)\\(", name = "methodName")
	})
	interface Source {}

	@Override
	public SourceFile getJniGeneratedHeaderFile() {
		return sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource(GreeterJniHeader.class, it -> {
			it.put("headerGuard", javaPackage.getName().replace(".", "_"));
			it.put("className", javaPackage.getName().replace(".", "_"));
			it.put("methodName", javaPackage.jniMethodName("Greeter", "sayHello"));
		}));
	}
}
