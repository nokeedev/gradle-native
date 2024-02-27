package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
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
		return new Source()
			.withPackage(javaPackage)
			.withPath("cpp/greeter.cpp")
			.getSourceFile();
	}

	@SourceFileLocation(file = "jni-cpp-greeter/src/main/cpp/greeter.cpp", properties = {
		@SourceFileProperty(regex = "^#include\\s+\"(com_example_greeter_Greeter.h)\"$", name = "jniHeader"),
		@SourceFileProperty(regex = "\\s+(Java_com_example_greeter_Greeter_sayHello)\\(", name = "methodName")
	})
	static class Source extends RegularFileContent {
		public Source withPackage(JavaPackage javaPackage) {
			properties.put("methodName", javaPackage.jniMethodName("Greeter", "sayHello"));
			properties.put("jniHeader", javaPackage.jniHeader("Greeter"));
			return this;
		}
	}

	@Override
	public SourceFile getJniGeneratedHeaderFile() {
		return new GreeterJniHeader().withPackage(javaPackage)
			.withPath("headers/" + javaPackage.jniHeader("Greeter")).getSourceFile();
	}
}
