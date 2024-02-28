package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaMainUsesGreeter extends SourceFileElement implements GreeterElement {
	private final SourceFile source;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "java-jni-app/src/main/java/com/example/app/Main.java", properties = {
		@SourceFileProperty(regex = "^package (com\\.example\\.app);$", name = "package")
	})
	static class Content extends RegularFileContent {
		public Content withPackage(JavaPackage javaPackage) {
			properties.put("package", javaPackage.getName());
			return this;
		}
	}

    public JavaMainUsesGreeter() {
		JavaPackage javaPackage = ofPackage("com.example.app");
		this.source = new Content().withPackage(javaPackage).withPath("java/" + javaPackage.getDirectoryLayout()).getSourceFile();
	}

	@Override
	public String getExpectedOutput() {
		return "Bonjour, World!";
	}
}
