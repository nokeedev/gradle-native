package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaGreeterJUnitTest extends SourceFileElement {
	private final SourceFile source;

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "java-jni-greeter/src/test/java/com/example/greeter/GreeterTest.java", properties = {
		@SourceFileProperty(regex = "^package (com\\.example\\.greeter);$", name = "package")
	})
	static class Source extends RegularFileContent {
		public Source withPackage(JavaPackage javaPackage) {
			properties.put("package", javaPackage.getName());
			return this;
		}
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	public JavaGreeterJUnitTest() {
		this(ofPackage("com.example.greeter"));
	}

	public JavaGreeterJUnitTest(JavaPackage javaPackage) {
		source = new Source().withPackage(javaPackage)
			.withPath("java/" + javaPackage.getDirectoryLayout() + "/GreeterTest.java").getSourceFile();
	}
}
