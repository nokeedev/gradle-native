package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class SwiftCompileGreeter extends SwiftSourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return new Source().withPath("swift/greeter.swift").getSourceFile();
	}

	@SourceFileLocation(file = "swift-compile-greeter/src/main/swift/greeter.swift")
	static class Source extends RegularFileContent {}
}
