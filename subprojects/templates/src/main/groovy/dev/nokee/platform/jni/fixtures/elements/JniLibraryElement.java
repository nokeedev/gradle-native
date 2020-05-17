package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;

import java.util.List;

public abstract class JniLibraryElement extends SourceElement {
	public abstract SourceElement getJvmSources();

	public abstract NativeSourceElement getNativeSources();

	@Override
	public List<SourceFile> getFiles() {
		return ofElements(getJvmSources(), getNativeSources()).getFiles();
	}

	@Override
	public void writeToProject(TestFile projectDir) {
		ofElements(getJvmSources(), getNativeSources()).writeToProject(projectDir);
	}

	public static NativeSourceElement ofNativeElements(NativeSourceElement... elements) {
		return NativeSourceElement.ofNativeElements(elements);
	}
}
