package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCppGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header().withPath("headers/greeter_fixtures.h");
	}

	@SourceFileLocation(file = "objcpp-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends RegularFileContent {}

	@Override
	public SourceFileElement getSource() {
		return new Source().withPath("objcpp/greeter_test.mm");
	}

	@SourceFileLocation(file = "objcpp-greeter-test/src/main/objcpp/greeter_test.mm")
	static class Source extends RegularFileContent {}
}
