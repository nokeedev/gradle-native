package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

public final class ObjectiveCppCompileGreeter extends SourceFileElement {
	@Override
	public SourceFile getSourceFile() {
		return new Source().withPath("objcpp/greeter.mm").getSourceFile();
	}

	@SourceFileLocation(file = "objcpp-compile-greeter/src/main/objcpp/greeter.mm")
	static class Source extends RegularFileContent {}
}
