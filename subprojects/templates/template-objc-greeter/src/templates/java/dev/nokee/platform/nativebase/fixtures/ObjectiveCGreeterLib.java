package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*ObjectiveCGreeter*/> {
	public final NativeLibraryElement delegate;

	public ObjectiveCGreeterLib() {
		super(new ObjectiveCGreetUsesGreeter().asLib(), new ObjectiveCGreeter().asLib());
		delegate = ofNativeLibraryElements((NativeLibraryElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter()));
	}

	private static class ObjectiveCGreetUsesGreeter extends NativeLibraryElement {
		private final SourceElement header;
		private final SourceElement source;

		@Override
		public SourceElement getPublicHeaders() {
			return header;
		}

		@Override
		public SourceElement getSources() {
			return source;
		}

		public ObjectiveCGreetUsesGreeter() {
			header = ofFile(sourceFile("headers", "greet_alice.h", fromResource("objc-greeter-lib/greet_alice.h")));
			source = ofFile(sourceFile("objc", "greet_alice_impl.m", fromResource("objc-greeter-lib/greet_alice_impl.m")));
		}
	}
}
