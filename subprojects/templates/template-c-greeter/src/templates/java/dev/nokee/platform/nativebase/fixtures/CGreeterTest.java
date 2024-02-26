package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource(Header.class)));
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/headers/greeter_fixtures.h")
	interface Header {}

	@Override
	public SourceFileElement getSource() {
		return ofFile(sourceFile("c", "greeter_test.c", fromResource(Source.class)));
	}

	@SourceFileLocation(file = "c-greeter-test/src/main/c/greeter_test.c")
	interface Source {}
}
