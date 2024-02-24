package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;

public final class SwiftGreeterTest extends SwiftSourceFileElement {
	private final SourceFile source;

	public SwiftGreeterTest(String testedModuleName) {
		this.source = sourceFile("swift", "greeter_test.swift", fromResource("swift-greeter-test/greeter_test.swift").replace("import SwiftGreeter", "import " + testedModuleName));
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFile getSourceFile() {
		return source;
	}
}
