package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

public final class ObjectiveCppCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return sourceFile("objcpp", "greeter.mm", fromResource("objcpp-compile-greeter/greeter.mm"));
	}
}
