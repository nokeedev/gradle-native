package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class CppGreeter extends NativeLibraryElement {
	private final SourceElement header;
	private final SourceElement source;

	@Override
	public SourceElement getPublicHeaders() {
		return header;
	}

	@Override
	public SourceElement getSources() {
		return source;
	}

	public CppGreeter() {
		header = ofFiles(sourceFile("headers", "greeter.h", fromResource("cpp-greeter/greeter.h")));
		source = ofFiles(sourceFile("cpp", "greeter_impl.cpp", fromResource("cpp-greeter/greeter_impl.cpp")));
	}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return header;
			}

			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("cpp", "greeter_impl.cpp", fromResource("cpp-greeter-with-optional-feature/greeter_impl.cpp")));
			}
		};
	}
}
