package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCppGreeterTest extends NativeSourceFileElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	@SourceFileLocation(file = "objcpp-greeter-test/src/main/headers/greeter_fixtures.h")
	public SourceFileElement getHeader() {
		return ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource("objcpp-greeter-test/greeter_fixtures.h")));
	}

	@Override
	@SourceFileLocation(file = "objcpp-greeter-test/src/main/objcpp/greeter_test.mm")
	public SourceFileElement getSource() {
		return ofFile(sourceFile("objcpp", "greeter_test.mm", fromResource("objcpp-greeter-test/greeter_test.mm")));
	}
}
