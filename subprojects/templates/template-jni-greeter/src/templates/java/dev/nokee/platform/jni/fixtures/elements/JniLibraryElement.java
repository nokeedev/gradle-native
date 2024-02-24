package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

import java.nio.file.Path;

import static dev.gradleplugins.fixtures.sources.SourceElement.ofElements;

public interface JniLibraryElement {
	SourceElement getJvmSources();

	SourceElement getNativeSources();

	SourceElement withJUnitTest();

	default void writeToProject(Path projectDir) {
		ofElements(getJvmSources(), getNativeSources()).writeToProject(projectDir);
	}

	default SourceElement withResources() {
		final SourceFileElement newResourceElement = new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile("resources", "foo.txt", "");
			}
		};
		return ofElements(getJvmSources(), newResourceElement, getNativeSources());
	}
}
