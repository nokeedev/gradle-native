package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class CCompileGreeter extends SourceFileElement {
	@Override
	@SourceFileLocation(file = "c-compile-greeter/src/main/c/greeter.c")
	public SourceFile getSourceFile() {
		return sourceFile("c", "greeter.c", fromResource("c-compile-greeter/greeter.c"));
	}
}
