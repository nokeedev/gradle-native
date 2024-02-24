package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniCppGreeterLib extends GreeterImplementationAwareSourceElement implements JniLibraryElement {
	private final CppGreeterJniBinding nativeBindings;
	private final JavaNativeGreeter jvmBindings;
	private final SourceElement jvmImplementation;
	private final CppGreeter nativeImplementation;
	private /*final*/ String projectName;
	private /*final*/ String resourcePath;

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

	public JavaJniCppGreeterLib(String projectName) {
		this(projectName, "");
	}

	public JavaJniCppGreeterLib(String projectName, String resourcePath) {
		this(ofPackage("com.example.greeter"), projectName, resourcePath);
		this.resourcePath = resourcePath;
		this.projectName = projectName;
	}

	private JavaJniCppGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath), new CppGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new CppGreeter());
	}

	private JavaJniCppGreeterLib(JavaNativeGreeter jvmBindings, CppGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, CppGreeter nativeImplementation) {
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

	public JavaJniCppGreeterLib withProjectName(String projectName) {
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	public JavaJniCppGreeterLib withResourcePath(String resourcePath) {
		assert resourcePath.endsWith("/");
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	public SourceElement withoutNativeImplementation() {
		return ofElements(getJvmSources(), nativeBindings);
	}

	public SourceElement withoutJvmImplementation() {
		return ofElements(jvmBindings, ofElements(nativeBindings, nativeImplementation));
	}

	public SourceElement withImplementationAsSubprojects() {
		return ofElements(
			jvmBindings.withSharedLibraryBaseName("cpp-jni-greeter").as(subproject("java-jni-greeter")),
			nativeBindings.withJniGeneratedHeader().as(subproject("cpp-jni-greeter")),
			jvmImplementation.as(subproject("java-loader")),
			nativeImplementation.asLib().as(subproject("cpp-greeter"))
		);
	}

	public SourceElement withOptionalFeature() {
		return ofElements(getJvmSources(),
			ofElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), nativeImplementation.as(lib()).as(subproject(subprojectPath)));
	}

	// withoutNativeImplementation().withJUnitTest()
	public SourceElement asSample() {
		return ofElements(withoutNativeImplementation(), new JavaGreeterJUnitTest());
	}
}

