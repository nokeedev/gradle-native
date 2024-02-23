package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class CppGreeterLib extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*CppGreeter*/> {
	private final NativeLibraryElement delegate;

    public CppGreeterLib() {
		super(new CppGreetUsingGreeter().asLib(), new CppGreeter().asLib());
		delegate = ofNativeLibraryElements((NativeLibraryElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter()));
	}

	public SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CppGreeterTest());
	}

	private static class CppGreetUsingGreeter extends NativeLibraryElement {
		private final SourceElement headers = SourceFileElement.ofFile(sourceFile("headers", "greet_alice.h", fromResource("cpp-greeter-lib/greet_alice.h")));
		private final SourceElement source = SourceFileElement.ofFile(sourceFile("cpp", "greet_alice.cpp", fromResource("cpp-greeter-lib/greet_alice.cpp")));

		@Override
		public SourceElement getPublicHeaders() {
			return headers;
		}

		@Override
		public SourceElement getSources() {
			return source;
		}
	}
}
