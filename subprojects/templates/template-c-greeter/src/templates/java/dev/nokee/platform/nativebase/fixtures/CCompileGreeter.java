package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class CCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return new Source().withPath("c").getSourceFile();
	}

	@SourceFileLocation(file = "c-compile-greeter/src/main/c/greeter.c")
	static class Source extends RegularFileContent {}
}
