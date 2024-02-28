package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class CppGreeter extends NativeSourceFileElement {
	@Override
	public SourceFileElement getHeader() {
		return new Header().withPath("headers");
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/public/greeter.h")
	static class Header extends RegularFileContent {}

	@Override
	public SourceFileElement getSource() {
		return new Source().withPath("cpp");
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/cpp/greeter_impl.cpp")
	static class Source extends RegularFileContent {}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeSourceFileElement() {
			@Override
			public SourceFileElement getHeader() {
				return CppGreeter.this.getHeader();
			}

			@Override
			public SourceFileElement getSource() {
				return new WithOptionalFeatureSource().withPath("cpp");
			}
		};
	}

	@SourceFileLocation(file = "cpp-greeter-with-optional-feature/src/main/cpp/greeter_impl.cpp")
	static class WithOptionalFeatureSource extends RegularFileContent {}
}
