package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.GreeterJniHeader;
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniCGreeterLib extends GreeterImplementationAwareSourceElement implements JniLibraryElement {
	private final CGreeterJniBinding nativeBindings;
	private final SourceElement jvmBindings;
	private final SourceElement jvmImplementation;
	private final CGreeter nativeImplementation;

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

	public JavaJniCGreeterLib(String projectName) {
		this(ofPackage("com.example.greeter"), projectName);
	}

	private JavaJniCGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName), new CGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new CGreeter());
	}

	private JavaJniCGreeterLib(JavaNativeGreeter jvmBindings, CGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, CGreeter nativeImplementation) {
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
	}

	@Override
	public SourceElement getElementUsingGreeter() {
		return ofElements(jvmBindings, jvmImplementation, nativeBindings);
	}

	@Override
	public SourceElement getGreeter() {
		return nativeImplementation;
	}

	public SourceElement withoutNativeImplementation() {
		return ofElements(getJvmSources(), nativeBindings);
	}

	public SourceElement withOptionalFeature() {
		return ofElements(getJvmSources(),
			ofElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), nativeImplementation.as(lib()).as(subproject(subprojectPath)));
	}

	private static class CGreeterJniBinding extends JniBindingElement {
		private final JavaPackage javaPackage;

		public CGreeterJniBinding(JavaPackage javaPackage) {
			this.javaPackage = javaPackage;
		}

		@Override
		public SourceFile getSourceFile() {
			return new Source().withPackage(javaPackage).withPath("c").getSourceFile();
		}

		@SourceFileLocation(file = "jni-c-greeter/src/main/c/greeter.c", properties = {
			@SourceFileProperty(regex = "^#include\\s+\"(com_example_greeter_Greeter.h)\"$", name = "jniHeader"),
			@SourceFileProperty(regex = "\\s+(Java_com_example_greeter_Greeter_sayHello)\\(", name = "methodName")
		})
		static class Source extends RegularFileContent {
			public Source withPackage(JavaPackage javaPackage) {
				properties.put("jniHeader", javaPackage.jniHeader("Greeter"));
				properties.put("methodName", javaPackage.jniMethodName("Greeter", "sayHello"));
				return this;
			}
		}

		@Override
		public SourceFile getJniGeneratedHeaderFile() {
			return new GreeterJniHeader().withPackage(javaPackage)
				.withPath("headers", javaPackage.jniHeader("Greeter")).getSourceFile();
		}
	}
}
