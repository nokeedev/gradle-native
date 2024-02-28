package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.ObjCppFileElement;

public final class ObjectiveCppGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header();
	}

	@SourceFileLocation(file = "objcpp-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends NativeHeaderFileElement {}

	@Override
	public SourceFileElement getSource() {
		return new Source();
	}

	@SourceFileLocation(file = "objcpp-greeter-test/src/main/objcpp/greeter_test.mm")
	static class Source extends ObjCppFileElement {}
}
