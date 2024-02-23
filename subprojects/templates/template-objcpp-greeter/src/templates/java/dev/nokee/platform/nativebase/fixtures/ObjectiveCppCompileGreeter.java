package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class ObjectiveCppCompileGreeter extends NativeSourceElement {
	@Override
	public SourceElement getSources() {
		return ofFiles(sourceFile("objcpp", "greeter.mmm", fromResource("objcpp-compile-greeter/greeter.mm")));
	}
}
