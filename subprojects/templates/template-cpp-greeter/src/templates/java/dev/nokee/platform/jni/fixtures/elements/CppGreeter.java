package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.CppFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;

public final class CppGreeter extends NativeSourceFileElement {
	@Override
	public SourceFileElement getHeader() {
		return new Header();
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/public/greeter.h")
	static class Header extends NativeHeaderFileElement {}

	@Override
	public SourceFileElement getSource() {
		return new Source();
	}

	@SourceFileLocation(file = "cpp-greeter/src/main/cpp/greeter_impl.cpp")
	static class Source extends CppFileElement {}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeSourceFileElement() {
			@Override
			public SourceFileElement getHeader() {
				return CppGreeter.this.getHeader();
			}

			@Override
			public SourceFileElement getSource() {
				return new WithOptionalFeatureSource();
			}
		};
	}

	@SourceFileLocation(file = "cpp-greeter-with-optional-feature/src/main/cpp/greeter_impl.cpp")
	static class WithOptionalFeatureSource extends CppFileElement {}
}
