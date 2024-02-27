package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;

public final class SwiftGreeterTest extends SwiftSourceFileElement {
	private final SourceFile source;

	public SwiftGreeterTest(String testedModuleName) {
		this.source = sourceFile("swift", "greeter_test.swift", fromResource(Source.class).replace("import SwiftGreeter", "import " + testedModuleName));
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "swift-greeter-test/src/main/swift/greeter_test.swift", properties = {
		@SourceFileProperty(regex = "^import (SwiftGreeter)$", name = "testedModuleName")
	})
	interface Source {}
}
