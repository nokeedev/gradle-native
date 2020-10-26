package dev.nokee.platform.jni.fixtures.elements;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

import java.io.File;
import java.util.List;

import static dev.gradleplugins.fixtures.sources.SourceElement.ofElements;

public interface JniLibraryElement {
	SourceElement getJvmSources();

	NativeSourceElement getNativeSources();

	default TestableJniLibraryElement withJUnitTest() {
		return new TestableJniLibraryElement(this, newJUnitTestElement());
	}

	default SourceElement newJUnitTestElement() {
		return new JavaGreeterJUnitTest();
	}

	default void writeToProject(File projectDir) {
		ofElements(getJvmSources(), getNativeSources()).writeToProject(projectDir);
	}

	static SourceFileElement newResourceElement() {
		return new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile("resources", "foo.txt", "");
			}
		};
	}

	default JniLibraryElement withResources() {
		return new SimpleJniLibraryElement(ofElements(getJvmSources(), JniLibraryElement.newResourceElement()), getNativeSources());
	}

	class SimpleJniLibraryElement extends SourceElement implements JniLibraryElement {
		private final SourceElement jvmSources;
		private final NativeSourceElement nativeSources;

		public SimpleJniLibraryElement(SourceElement jvmSources, NativeSourceElement nativeSources) {
			this.jvmSources = jvmSources;
			this.nativeSources = nativeSources;
		}

		@Override
		public List<SourceFile> getFiles() {
			return ImmutableList.<SourceFile>builder().addAll(getJvmSources().getFiles()).addAll(getNativeSources().getFiles()).build();
		}

		@Override
		public SourceElement getJvmSources() {
			return jvmSources;
		}

		@Override
		public NativeSourceElement getNativeSources() {
			return nativeSources;
		}

		@Override
		public void writeToProject(File projectDir) {
			jvmSources.writeToProject(projectDir);
			nativeSources.writeToProject(projectDir);
		}
	}
}
