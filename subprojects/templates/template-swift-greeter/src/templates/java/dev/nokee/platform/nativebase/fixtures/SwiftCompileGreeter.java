package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

public final class SwiftCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return sourceFile("swift", "greeter.swift", fromResource("swift-compile-greeter/src/main/swift/greeter.swift"));
	}
}
