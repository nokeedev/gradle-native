package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return new Source().withPath("objc/greeter.m").getSourceFile();
	}

	@SourceFileLocation(file = "objc-compile-greeter/src/main/objc/greeter.m")
	static class Source extends RegularFileContent {}
}
