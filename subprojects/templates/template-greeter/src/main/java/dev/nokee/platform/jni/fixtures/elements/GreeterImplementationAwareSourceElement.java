/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GreeterImplementationAwareSourceElement<T extends SourceElement> extends SourceElement {
	private final SourceElement elementUsingGreeter;
	private final T greeter;

	public GreeterImplementationAwareSourceElement(SourceElement elementUsingGreeter, T greeter) {
		this.elementUsingGreeter = elementUsingGreeter;
		this.greeter = greeter;
	}

	@Override
	public List<SourceFile> getFiles() {
		return Stream.concat(elementUsingGreeter.getFiles().stream(), greeter.getFiles().stream()).collect(Collectors.toList());
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
				delegate.writeToProject(new File(projectDir, subprojectPath));
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
				delegate.writeToProject(new File(projectDir, subprojectPath));
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
