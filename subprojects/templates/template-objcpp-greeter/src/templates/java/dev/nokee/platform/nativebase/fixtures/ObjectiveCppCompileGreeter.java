package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCppCompileGreeter extends SourceFileElement {
	@Override
	@SourceFileLocation(file = "objcpp-compile-greeter/src/main/objcpp/greeter.mm")
	public SourceFile getSourceFile() {
		return sourceFile("objcpp", "greeter.mm", fromResource("objcpp-compile-greeter/greeter.mm"));
	}
}
