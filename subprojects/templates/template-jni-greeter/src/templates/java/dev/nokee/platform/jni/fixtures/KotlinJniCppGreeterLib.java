package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class KotlinJniCppGreeterLib extends GreeterImplementationAwareSourceElement implements JniLibraryElement {
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
	public SourceElement getNativeSources() {
		return ofElements(nativeBindings, nativeImplementation);
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
		this.jvmBindings = jvmBindings;
		this.nativeBindings = nativeBindings;
		this.jvmImplementation = jvmImplementation;
		this.nativeImplementation = nativeImplementation;
		this.junitTest = junitTest;
	}

	@Override
	public SourceElement getElementUsingGreeter() {
		return ofElements(jvmBindings, nativeBindings, jvmImplementation);
	}

	@Override
	public SourceElement getGreeter() {
		return nativeImplementation;
	}

	@Override
	public SourceElement withJUnitTest() {
		return ofElements(this, junitTest);
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), nativeImplementation.as(lib()).as(subproject(subprojectPath)));
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

		@SourceFileLocation(file = "kotlin-jni-greeter/src/main/kotlin/com/example/greeter/Greeter.kt", properties = {
			@SourceFileProperty(regex = "^package (com\\.example\\.greeter)$", name = "package"),
			@SourceFileProperty(regex = "\"(\\$\\{resourcePath\\}\\$\\{sharedLibraryBaseName\\})\"", name = "libName")
		})
		interface Content {}

		public KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName) {
			this(javaPackage, sharedLibraryBaseName, "");
		}

		public KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
			this.javaPackage = javaPackage;
			this.sharedLibraryBaseName = sharedLibraryBaseName;
			this.resourcePath = resourcePath;
			source = sourceFile("kotlin/" + javaPackage.getDirectoryLayout(), "Greeter.kt", fromResource(Content.class, it -> {
				it.put("package", javaPackage.getName());
				it.put("libName", resourcePath + sharedLibraryBaseName);
			}));
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
			source = new Content().withPackage(javaPackage).withPath("kotlin/" + javaPackage.getDirectoryLayout()).getSourceFile();
		}

		@SourceFileLocation(file = "kotlin-jni-greeter/src/main/kotlin/com/example/greeter/NativeLoader.kt", properties = {
			@SourceFileProperty(regex = "^package (com\\.example\\.greeter)$", name = "package"),
		})
		static class Content extends RegularFileContent {
			public Content withPackage(JavaPackage javaPackage) {
				properties.put("package", javaPackage.getName());
				return this;
			}
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
			this.source = new Content().withPackage(javaPackage).withPath("kotlin/" + javaPackage.getDirectoryLayout()).getSourceFile();
		}

		@SourceFileLocation(file = "kotlin-jni-greeter/src/test/kotlin/com/example/greeter/GreeterTest.kt", properties = {
			@SourceFileProperty(regex = "^package (com\\.example\\.greeter)$", name = "package"),
		})
		static class Content extends RegularFileContent {
			public Content withPackage(JavaPackage javaPackage) {
				properties.put("package", javaPackage.getName());
				return this;
			}
		}
	}
}
