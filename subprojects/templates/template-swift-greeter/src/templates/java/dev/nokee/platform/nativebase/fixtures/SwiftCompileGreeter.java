package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class SwiftCompileGreeter extends SwiftSourceFileElement {
	@Override
	@SourceFileLocation(file = "swift-compile-greeter/src/main/swift/greeter.swift")
	public SourceFile getSourceFile() {
		return sourceFile("swift", "greeter.swift", fromResource("swift-compile-greeter/greeter.swift"));
	}
}
