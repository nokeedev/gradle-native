package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CGreeterTest extends NativeSourceElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceElement getHeaders() {
		return ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource("c-greeter-test/greeter_fixtures.h")));
	}

	@Override
	public SourceElement getSources() {
		return ofFiles(sourceFile("c", "greeter_test.c", fromResource("c-greeter-test/greeter_test.c")));
	}
}
