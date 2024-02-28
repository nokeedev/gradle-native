package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

public final class JavaNativeGreeter extends SourceFileElement {
	private final SourceFile source;
	private final JavaPackage javaPackage;
	private final String sharedLibraryBaseName;
	private final String resourcePath;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "java-jni-greeter/src/main/java/com/example/greeter/Greeter.java", properties = {
		@SourceFileProperty(regex = "^package (com\\.example\\.greeter);$", name = "package"),
		@SourceFileProperty(regex = "\"(\\$\\{resourcePath\\}\\$\\{sharedLibraryBaseName\\})\"", name = "libName")
	})
	static class Content extends RegularFileContent {
		public Content withPackage(JavaPackage javaPackage) {
			properties.put("package", javaPackage.getName());
			return this;
		}

		public Content withLibName(String s) {
			properties.put("libName", s);
			return this;
		}
	}

	public JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName) {
		this(javaPackage, sharedLibraryBaseName, "");
	}

	public JavaNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this.javaPackage = javaPackage;
		this.sharedLibraryBaseName = sharedLibraryBaseName;
		this.resourcePath = resourcePath;
		source = new Content().withPackage(javaPackage).withLibName(resourcePath + sharedLibraryBaseName).withPath("java/" + javaPackage.getDirectoryLayout()).getSourceFile();
	}

	public JavaNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
	}

	public JavaNativeGreeter withResourcePath(String resourcePath) {
		return new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath);
	}
}
