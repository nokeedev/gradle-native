package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header().withPath("headers/greeter_fixtures.h");
	}

	@SourceFileLocation(file = "objc-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends RegularFileContent {}

	@Override
	public SourceFileElement getSource() {
		return new Source().withPath("objc/greeter_test.m");
	}

	@SourceFileLocation(file = "objc-greeter-test/src/main/objc/greeter_test.m")
	static class Source extends RegularFileContent {}
}
