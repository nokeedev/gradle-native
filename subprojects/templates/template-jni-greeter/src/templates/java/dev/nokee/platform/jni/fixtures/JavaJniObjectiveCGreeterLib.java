package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	private final ObjectiveCGreeterJniBinding nativeBindings;
	private final SourceElement jvmBindings;
	private final SourceElement jvmImplementation;
	private final ObjectiveCGreeter nativeImplementation;

	@Override
	public SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation);
	}

	@Override
	public NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	public JavaJniObjectiveCGreeterLib(String projectName) {
		this(ofPackage("com.example.greeter"), projectName);
	}

	private JavaJniObjectiveCGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName), new ObjectiveCGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new ObjectiveCGreeter());
	}

	private JavaJniObjectiveCGreeterLib(JavaNativeGreeter jvmBindings, ObjectiveCGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, ObjectiveCGreeter nativeImplementation) {
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation);
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
	}

	public JniLibraryElement withoutNativeImplementation() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation), nativeBindings);
	}

	public JniLibraryElement withFoundationFrameworkDependency() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation, newJUnitTestElement()), ofNativeElements(nativeBindings, nativeImplementation.withFoundationFrameworkImplementation()));
	}

	public JniLibraryElement withOptionalFeature() {
		return new SimpleJniLibraryElement(getJvmSources(), ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, nativeImplementation.asLib()));
	}

	private static class ObjectiveCGreeterJniBinding extends NativeSourceElement {
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

		ObjectiveCGreeterJniBinding(JavaPackage javaPackage) {
			this.javaPackage = javaPackage;
			source = ofFiles(sourceFile("objc", "greeter.m", fromResource("jni-objc-greeter/greeter.m").replace(ofPackage("com.example.greeter").jniHeader("Greeter"), javaPackage.jniHeader("Greeter")).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello"))));
			generatedHeader = ofFiles(sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource("java-jni-greeter/com_example_greeter_Greeter.h").replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello"))));
		}

		public SourceElement withJniGeneratedHeader() {
			return new NativeSourceElement() {
				@Override
				public SourceElement getHeaders() {
					return generatedHeader;
				}

				@Override
				public SourceElement getSources() {
					return ObjectiveCGreeterJniBinding.this.source;
				}
			};
		}
	}
}
