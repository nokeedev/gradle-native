package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
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
	@SourceFileLocation(file = "c-greeter-test/src/main/headers/greeter_fixtures.h")
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource("c-greeter-test/greeter_fixtures.h")));
	}

	@Override
	@SourceFileLocation(file = "c-greeter-test/src/main/c/greeter_test.c")
	public SourceFileElement getSource() {
		return ofFile(sourceFile("c", "greeter_test.c", fromResource("c-greeter-test/greeter_test.c")));
	}
}
