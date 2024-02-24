package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;

public final class SwiftCompileGreeter extends SwiftSourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return sourceFile("swift", "greeter.swift", fromResource("swift-compile-greeter/src/main/swift/greeter.swift"));
	}
}
