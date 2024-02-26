package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return sourceFile("objc", "greeter.m", fromResource(Source.class));
	}

	@SourceFileLocation(file = "objc-compile-greeter/src/main/objc/greeter.m")
	interface Source {}
}
