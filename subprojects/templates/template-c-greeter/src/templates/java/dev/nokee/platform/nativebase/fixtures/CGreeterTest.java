package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.CFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;

public final class CGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header();
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends NativeHeaderFileElement {}

	@Override
	public SourceFileElement getSource() {
		return new Source();
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/c/greeter_test.c")
	static class Source extends CFileElement {}
}
