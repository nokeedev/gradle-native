package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;
import dev.nokee.platform.jni.fixtures.CGreeter;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniCGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	private final CGreeterJniBinding nativeBindings;
	private final SourceElement jvmBindings;
	private final SourceElement jvmImplementation;
	private final CGreeter nativeImplementation;

	@Override
	public SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation);
	}

	@Override
	public NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	public JavaJniCGreeterLib(String projectName) {
		this(ofPackage("com.example.greeter"), projectName);
	}

	private JavaJniCGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName), new CGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new CGreeter());
	}

	private JavaJniCGreeterLib(JavaNativeGreeter jvmBindings, CGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, CGreeter nativeImplementation) {
		super(ofElements(jvmBindings, jvmImplementation, nativeBindings), nativeImplementation);
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
	}

	public JniLibraryElement withoutNativeImplementation() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation), nativeBindings);
	}

	public JniLibraryElement withOptionalFeature() {
		return new SimpleJniLibraryElement(getJvmSources(), ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, nativeImplementation.asLib()));
	}

	private static class CGreeterJniBinding extends NativeSourceElement {
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

		public CGreeterJniBinding(JavaPackage javaPackage) {
			this.javaPackage = javaPackage;
			source = ofFile(sourceFile("c", "greeter.c", fromResource("jni-c-greeter/greeter.c").replace(ofPackage("com.example.greeter").jniHeader("Greeter"), javaPackage.jniHeader("Greeter")).replace(ofPackage("com.example.greeter").jniMethodName("Greeter", "sayHello"), javaPackage.jniMethodName("Greeter", "sayHello"))));
			generatedHeader = ofFile(sourceFile("headers", javaPackage.jniHeader("Greeter"), fromResource("java-jni-greeter/" + ofPackage("com.example.greeter").jniHeader("Greeter"))));
		}

		public SourceElement withJniGeneratedHeader() {
			return new NativeSourceElement() {
				@Override
				public SourceElement getHeaders() {
					return generatedHeader;
				}

				@Override
				public SourceElement getSources() {
					return CGreeterJniBinding.this.source;
				}
			};
		}
	}
}
