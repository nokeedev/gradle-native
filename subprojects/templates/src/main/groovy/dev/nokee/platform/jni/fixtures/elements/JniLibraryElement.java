package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;
import dev.gradleplugins.test.fixtures.sources.SourceFileElement;

import java.util.List;

public abstract class JniLibraryElement extends SourceElement {
	public abstract SourceElement getJvmSources();

	public abstract NativeSourceElement getNativeSources();

	public TestableJniLibraryElement withJUnitTest() {
		return new TestableJniLibraryElement(this, newJUnitTestElement());
	}

	protected SourceElement newJUnitTestElement() {
		return new JavaGreeterJUnitTest();
	}

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

	protected SourceFileElement newResourceElement() {
		return new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile("resources", "foo.txt", "");
			}
		};
	}

	public JniLibraryElement withResources() {
		return new JniLibraryElement() {
			@Override
			public SourceElement getJvmSources() {
				return ofElements(JniLibraryElement.this.getJvmSources(), JniLibraryElement.this.newResourceElement());
			}

			@Override
			public NativeSourceElement getNativeSources() {
				return JniLibraryElement.this.getNativeSources();
			}
		};
	}
}
