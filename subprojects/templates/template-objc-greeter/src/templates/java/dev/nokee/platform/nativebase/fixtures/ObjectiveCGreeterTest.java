package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	@SourceFileLocation(file = "objc-greeter-test/src/main/headers/greeter_fixtures.h")
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource("objc-greeter-test/greeter_fixtures.h")));
	}

	@Override
	@SourceFileLocation(file = "objc-greeter-test/src/main/objc/greeter_test.m")
	public SourceFileElement getSource() {
		return ofFile(sourceFile("objc", "greeter_test.m", fromResource("objc-greeter-test/greeter_test.m")));
	}
}
