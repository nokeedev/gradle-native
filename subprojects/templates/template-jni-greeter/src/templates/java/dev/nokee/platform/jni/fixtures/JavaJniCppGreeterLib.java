package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeGreeter;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;

import java.io.File;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniCppGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
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
	public NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
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
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation);
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
	}

	public JavaJniCppGreeterLib withProjectName(String projectName) {
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	public JavaJniCppGreeterLib withResourcePath(String resourcePath) {
		assert resourcePath.endsWith("/");
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	public JniLibraryElement withoutNativeImplementation() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation), nativeBindings);
	}

	public JniLibraryElement withoutJvmImplementation() {
		return new SimpleJniLibraryElement(jvmBindings, ofNativeElements(nativeBindings, nativeImplementation));
	}

	public JniLibraryElement withImplementationAsSubprojects() {
		return new SimpleJniLibraryElement(null, null) {
			@Override
			public SourceElement getJvmSources() {
				throw new UnsupportedOperationException();
			}

			@Override
			public NativeSourceElement getNativeSources() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void writeToProject(File projectDir) {
				jvmBindings.withSharedLibraryBaseName("cpp-jni-greeter").writeToProject(new File(projectDir, "java-jni-greeter"));
				nativeBindings.withJniGeneratedHeader().writeToProject(new File(projectDir, "cpp-jni-greeter"));
				jvmImplementation.writeToProject(new File(projectDir, "java-loader"));
				nativeImplementation.asLib().writeToProject(new File(projectDir, "cpp-greeter"));
			}
		};
	}

	public JniLibraryElement withOptionalFeature() {
		return new SimpleJniLibraryElement(getJvmSources(), ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature()));
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, nativeImplementation.asLib()));
	}
}

