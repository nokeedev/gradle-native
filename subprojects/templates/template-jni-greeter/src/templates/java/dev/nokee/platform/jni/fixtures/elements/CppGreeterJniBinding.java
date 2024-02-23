package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public class CppGreeterJniBinding extends NativeSourceElement {
	private final SourceElement source;
	private final SourceElement generatedHeader;
	private final JavaPackage javaPackage;

	@Override
	public SourceElement getHeaders() {
		return empty();
	}

	@Override
	public SourceElement getSources() {
		return source;
	}

    public CppGreeterJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage;
		source = ofFiles(sourceFile("cpp", "greeter.cpp", fromResource("jni-cpp-greeter/greeter.cpp").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello"))));
		generatedHeader = ofFiles(sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource("java-jni-greeter/com_example_greeter_Greeter.h").replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello"))));
	}

	public NativeSourceElement withJniGeneratedHeader() {
		return new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return generatedHeader;
			}

			@Override
			public SourceElement getSources() {
				return CppGreeterJniBinding.this.source;
			}
		};
	}
}
