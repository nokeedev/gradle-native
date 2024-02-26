package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CppGreeter extends NativeSourceFileElement {
	@Override
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter.h", fromResource(Header.class)));
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/public/greeter.h")
	interface Header {}

	@Override
	public SourceFileElement getSource() {
		return ofFile(sourceFile("cpp", "greeter_impl.cpp", fromResource(Source.class)));
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/cpp/greeter_impl.cpp")
	interface Source {}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeSourceFileElement() {
			@Override
			public SourceFileElement getHeader() {
				return CppGreeter.this.getHeader();
			}

			@Override
			public SourceFileElement getSource() {
				return ofFile(sourceFile("cpp", "greeter_impl.cpp", fromResource(WithOptionalFeatureSource.class)));
			}
		};
	}

	@SourceFileLocation(file = "cpp-greeter-with-optional-feature/src/main/cpp/greeter_impl.cpp")
	interface WithOptionalFeatureSource {}
}
