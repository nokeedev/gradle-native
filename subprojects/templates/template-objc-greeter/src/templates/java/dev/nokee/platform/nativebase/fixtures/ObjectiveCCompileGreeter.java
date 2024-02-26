package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCCompileGreeter extends SourceFileElement {
	@Override
	@SourceFileLocation(file = "objc-compile-greeter/src/main/objc/greeter.m")
	public SourceFile getSourceFile() {
		return sourceFile("objc", "greeter.m", fromResource("objc-compile-greeter/greeter.m"));
	}
}
