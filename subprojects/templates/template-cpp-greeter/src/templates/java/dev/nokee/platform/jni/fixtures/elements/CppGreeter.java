package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CppGreeter extends NativeSourceFileElement {
	@Override
	@SourceFileLocation(file = "cpp-greeter/src/main/public/greeter.h")
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter.h", fromResource("cpp-greeter/greeter.h")));
	}

	@Override
	@SourceFileLocation(file = "cpp-greeter/src/main/cpp/greeter_impl.cpp")
	public SourceFileElement getSource() {
		return ofFile(sourceFile("cpp", "greeter_impl.cpp", fromResource("cpp-greeter/greeter_impl.cpp")));
	}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeSourceFileElement() {
			@Override
			public SourceFileElement getHeader() {
				return CppGreeter.this.getHeader();
			}

			@Override
			@SourceFileLocation(file = "cpp-greeter-with-optional-feature/src/main/cpp/greeter_impl.cpp")
			public SourceFileElement getSource() {
				return ofFile(sourceFile("cpp", "greeter_impl.cpp", fromResource("cpp-greeter-with-optional-feature/greeter_impl.cpp")));
			}
		};
	}
}
