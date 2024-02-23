package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCGreeterTest extends NativeSourceElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceElement getHeaders() {
		return ofFiles(sourceFile("headers", "greeter_fixtures.h", fromResource("objc-greeter-test/greeter_fixtures.h")));
	}

	@Override
	public SourceElement getSources() {
		return ofFile(sourceFile("objc", "greeter_test.m", fromResource("objc-greeter-test/greeter_test.m")));
	}
}
