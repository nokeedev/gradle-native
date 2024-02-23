package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CCompileGreeter extends NativeSourceElement {
	@Override
	public SourceElement getSources() {
		return ofFile(sourceFile("c", "greeter.c", fromResource("c-compile-greeter/greeter.c")));
	}
}
