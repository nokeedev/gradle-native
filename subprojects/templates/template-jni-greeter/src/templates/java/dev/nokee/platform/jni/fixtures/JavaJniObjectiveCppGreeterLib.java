package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniObjectiveCppGreeterLib  extends GreeterImplementationAwareSourceElement implements JniLibraryElement {
	private final ObjectiveCppGreeterJniBinding nativeBindings;
	private final SourceElement jvmBindings;
	private final SourceElement jvmImplementation;
	private final ObjectiveCppGreeter nativeImplementation;

	@Override
	public SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation);
	}

	@Override
	public SourceElement getNativeSources() {
		return ofElements(nativeBindings, nativeImplementation);
	}

	@Override
	public SourceElement withJUnitTest() {
		return ofElements(this, new JavaGreeterJUnitTest());
	}

	public JavaJniObjectiveCppGreeterLib(String projectName) {
		this(ofPackage("com.example.greeter"), projectName);
	}

	private JavaJniObjectiveCppGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName), new ObjectiveCppGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new ObjectiveCppGreeter());
	}

	private JavaJniObjectiveCppGreeterLib(JavaNativeGreeter jvmBindings, ObjectiveCppGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, ObjectiveCppGreeter nativeImplementation) {
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
	}

	@Override
	public SourceElement getElementUsingGreeter() {
		return ofElements(jvmBindings, nativeBindings, jvmImplementation);
	}

	@Override
	public SourceElement getGreeter() {
		return nativeImplementation;
	}

	public SourceElement withoutNativeImplementation() {
		return ofElements(getJvmSources(), nativeBindings);
	}

	public SourceElement withOptionalFeature() {
		return ofElements(getJvmSources(), ofElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), nativeImplementation.as(lib()).as(subproject(subprojectPath)));
	}

	private static class ObjectiveCppGreeterJniBinding extends JniBindingElement {
		private final JavaPackage javaPackage;

		public ObjectiveCppGreeterJniBinding(JavaPackage javaPackage) {
			this.javaPackage = javaPackage;
		}

		@Override
		@SourceFileLocation(file = "jni-objcpp-greeter/src/main/objcpp/greeter.mm")
		public SourceFile getSourceFile() {
			return sourceFile("objcpp", "greeter.mm", fromResource("jni-objcpp-greeter/greeter.mm").replace(ofPackage("com.example.greeter").jniHeader("Greeter"), javaPackage.jniHeader("Greeter")).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
		}

		@Override
		@SourceFileLocation(file = "java-jni-greeter/src/main/headers/com_example_greeter_Greeter.h")
		public SourceFile getJniGeneratedHeaderFile() {
			return sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource("java-jni-greeter/com_example_greeter_Greeter.h").replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello")));
		}
	}
}
