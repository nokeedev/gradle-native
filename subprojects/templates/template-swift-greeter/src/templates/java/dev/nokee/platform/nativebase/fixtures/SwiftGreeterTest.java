package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.nativebase.SwiftFileElement;

@SourceFileLocation(file = "swift-greeter-test/src/main/swift/greeter_test.swift", properties = {
	@SourceFileProperty(regex = "^import (SwiftGreeter)$", name = "testedModuleName")
})
public final class SwiftGreeterTest extends SwiftFileElement {
	public SwiftGreeterTest(String testedModuleName) {
		properties.put("testedModuleName", testedModuleName);
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}
}
