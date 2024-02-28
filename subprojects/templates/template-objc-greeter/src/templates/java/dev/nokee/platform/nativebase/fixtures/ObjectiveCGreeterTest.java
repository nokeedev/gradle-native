package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.ObjCFileElement;

public final class ObjectiveCGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return new Header();
	}

	@SourceFileLocation(file = "objc-greeter-test/src/main/headers/greeter_fixtures.h")
	static class Header extends NativeHeaderFileElement {}

	@Override
	public SourceFileElement getSource() {
		return new Source();
	}

	@SourceFileLocation(file = "objc-greeter-test/src/main/objc/greeter_test.m")
	static class Source extends ObjCFileElement {}
}
