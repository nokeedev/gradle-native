package dev.nokee.platform.jni.fixtures.elements;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public abstract class GreeterImplementationAwareSourceElement<T extends SourceElement> extends SourceElement {
	private final SourceElement elementUsingGreeter;
	private final T greeter;

	public GreeterImplementationAwareSourceElement(SourceElement elementUsingGreeter, T greeter) {
		this.elementUsingGreeter = elementUsingGreeter;
		this.greeter = greeter;
	}

	@Override
	public List<SourceFile> getFiles() {
		return ImmutableList.<SourceFile>builder().addAll(elementUsingGreeter.getFiles()).addAll(greeter.getFiles()).build();
	}

	public SourceElement getElementUsingGreeter() {
		return elementUsingGreeter;
	}

	public T getGreeter() {
		return greeter;
	}

	@Override
	public void writeToProject(File projectDir) {
		ofElements(elementUsingGreeter, greeter).writeToProject(projectDir);
	}

	public abstract GreeterImplementationAwareSourceElement<T> withImplementationAsSubproject(String subprojectPath);

	protected <U extends SourceElement> GreeterImplementationAwareSourceElement<U> ofImplementationAsSubproject(SourceElement elementUsingGreeter, U greeter) {
		return new GreeterImplementationAwareSourceElement<U>(elementUsingGreeter, greeter) {
			@Override
			public GreeterImplementationAwareSourceElement<U> withImplementationAsSubproject(String subprojectPath) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static SourceElement asSubproject(String subprojectPath, SourceElement delegate) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return delegate.getFiles();
			}

			@Override
			public void writeToProject(File projectDir) {
				delegate.writeToProject(file(projectDir, subprojectPath));
			}
		};
	}

	public static NativeLibraryElement asSubproject(String subprojectPath, NativeLibraryElement delegate) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return asSubproject(subprojectPath, delegate.getPublicHeaders());
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return asSubproject(subprojectPath, delegate.getPrivateHeaders());
			}

			@Override
			public SourceElement getSources() {
				return asSubproject(subprojectPath, delegate.getSources());
			}

			@Override
			public void writeToProject(File projectDir) {
				delegate.writeToProject(file(projectDir, subprojectPath));
			}
		};
	}

	public static NativeLibraryElement ofNativeLibraryElements(final NativeLibraryElement... elements) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return ofElements(Arrays.stream(elements).map(NativeLibraryElement::getPublicHeaders).collect(Collectors.toList()));
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return ofElements(Arrays.stream(elements).map(NativeLibraryElement::getPrivateHeaders).collect(Collectors.toList()));
			}

			@Override
			public SourceElement getSources() {
				return ofElements(Arrays.stream(elements).map(NativeSourceElement::getSources).collect(Collectors.toList()));
			}

			@Override
			public List<SourceFile> getFiles() {
				List<SourceFile> files = new ArrayList<SourceFile>();
				for (SourceElement element : elements) {
					files.addAll(element.getFiles());
				}
				return files;
			}

			@Override
			public void writeToProject(File projectDir) {
				for (SourceElement element : elements) {
					element.writeToProject(projectDir);
				}
			}
		};
	}
}
