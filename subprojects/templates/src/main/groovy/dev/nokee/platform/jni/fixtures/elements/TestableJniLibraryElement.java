package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;

import java.util.List;

public class TestableJniLibraryElement extends JniLibraryElement {
	private final JniLibraryElement main;
	private final SourceElement test;

	public TestableJniLibraryElement(JniLibraryElement main, SourceElement test) {
		this.main = main;
		this.test = test;
	}

	@Override
	public SourceElement getJvmSources() {
		return ofElements(main.getJvmSources(), test);
	}

	@Override
	public NativeSourceElement getNativeSources() {
		return main.getNativeSources();
	}

	@Override
	public TestableJniLibraryElement withJUnitTest() {
		return this;
	}

	@Override
	public List<SourceFile> getFiles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeToProject(TestFile projectDir) {
		main.writeToProject(projectDir);
		test.writeToProject(projectDir);
	}
}
