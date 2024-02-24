package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

public final class CCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return sourceFile("c", "greeter.c", fromResource("c-compile-greeter/greeter.c"));
	}
}
