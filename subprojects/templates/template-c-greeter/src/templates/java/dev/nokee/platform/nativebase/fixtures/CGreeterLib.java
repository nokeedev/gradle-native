package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.CGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CGreeterLib extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*CGreeter*/> {
	private final NativeLibraryElement delegate;

	public CGreeterLib() {
		super(new CGreetUsingGreeter().asLib(), new CGreeter().asLib());
		delegate = ofNativeLibraryElements((NativeLibraryElement) getElementUsingGreeter(), getGreeter());
	}

	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter()));
	}

	public SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CGreeterTest());
	}

	private static class CGreetUsingGreeter extends NativeLibraryElement {
		@Override
		public SourceElement getPublicHeaders() {
			return ofFile(sourceFile("headers", "greet_alice.h", fromResource("c-greeter-lib/greet_alice.h")));
		}

		@Override
		public SourceElement getSources() {
			return ofFile(sourceFile("c", "greet_alice.c", fromResource("c-greeter-lib/greet_alice.c")));
		}
	}
}
