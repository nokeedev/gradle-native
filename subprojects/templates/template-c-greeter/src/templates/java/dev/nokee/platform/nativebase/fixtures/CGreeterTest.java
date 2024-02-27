package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class CGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header().withPath("headers/greeter_fixtures.h");
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends RegularFileContent {}

	@Override
	public SourceFileElement getSource() {
		return new Source().withPath("c/greeter_test.c");
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/c/greeter_test.c")
	static class Source extends RegularFileContent {}
}
