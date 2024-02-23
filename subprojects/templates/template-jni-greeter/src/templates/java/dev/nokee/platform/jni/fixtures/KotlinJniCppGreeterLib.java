package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;
import dev.nokee.platform.jni.fixtures.elements.TestableJniLibraryElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class KotlinJniCppGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	private final NativeSourceElement nativeBindings;
	private final KotlinNativeGreeter jvmBindings;
	private final SourceElement jvmImplementation;
	private final CppGreeter nativeImplementation;
	private final SourceElement junitTest;
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

	public KotlinJniCppGreeterLib(String projectName) {
		this(projectName, "");
	}

	public KotlinJniCppGreeterLib(String projectName, String resourcePath) {
		this(ofPackage("com.example.greeter"), projectName, resourcePath);
		this.resourcePath = resourcePath;
		this.projectName = projectName;
	}

	private KotlinJniCppGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this(new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath), new CppGreeterJniBinding(javaPackage).withJniGeneratedHeader(), new KotlinNativeLoader(javaPackage), new CppGreeter(), new KotlinGreeterJUnitTest(javaPackage));
	}

	private KotlinJniCppGreeterLib(KotlinNativeGreeter jvmBindings, NativeSourceElement nativeBindings, KotlinNativeLoader jvmImplementation, CppGreeter nativeImplementation, KotlinGreeterJUnitTest junitTest) {
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation);
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
		this.junitTest = junitTest;
	}

	@Override
	public TestableJniLibraryElement withJUnitTest() {
		return new TestableJniLibraryElement(this, junitTest);
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, nativeImplementation.asLib()));
	}

	private static class KotlinNativeGreeter extends SourceFileElement {
		private final SourceFile source;
		private final JavaPackage javaPackage;
		private final String sharedLibraryBaseName;
		private final String resourcePath;

		@Override
		public SourceFile getSourceFile() {
			return source;
		}

		public KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName) {
			this(javaPackage, sharedLibraryBaseName, "");
		}

		public KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
			this.javaPackage = javaPackage;
			this.sharedLibraryBaseName = sharedLibraryBaseName;
			this.resourcePath = resourcePath;
			source = sourceFile("kotlin/" + javaPackage.getDirectoryLayout(), "Greeter.kt", fromResource("kotlin-jni-greeter/Greeter.kt").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()).replace("${resourcePath}${sharedLibraryBaseName}", resourcePath + sharedLibraryBaseName));
		}

		public KotlinNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
			return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
		}

		public KotlinNativeGreeter withResourcePath(String resourcePath) {
			return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
		}
	}

	private static class KotlinNativeLoader extends SourceFileElement {
		private final SourceFile source;

		@Override
		public SourceFile getSourceFile() {
			return source;
		}

		public KotlinNativeLoader(JavaPackage javaPackage) {
			source = sourceFile("kotlin/" + javaPackage.getDirectoryLayout(), "NativeLoader.kt", fromResource("kotlin-jni-greeter/NativeLoader.kt").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()));
		}
	}

	private static class KotlinGreeterJUnitTest extends SourceFileElement {
		private final SourceFile source;

		@Override
		public SourceFile getSourceFile() {
			return source;
		}

		@Override
		public String getSourceSetName() {
			return "test";
		}

		public KotlinGreeterJUnitTest(JavaPackage javaPackage) {
			source = sourceFile("kotlin/" + javaPackage.getDirectoryLayout(), "GreeterTest.kt", fromResource("kotlin-jni-greeter/GreeterTest.kt").replace("package " + ofPackage("com.example.greeter").getName(), "package " + javaPackage.getName()));
		}
	}
}
