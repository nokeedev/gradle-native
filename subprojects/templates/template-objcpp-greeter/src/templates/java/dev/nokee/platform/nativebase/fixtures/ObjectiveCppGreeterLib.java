package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCppGreeterLib extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*ObjectiveCppGreeter*/> {
	public final NativeLibraryElement delegate;

	public ObjectiveCppGreeterLib() {
		super(new ObjectiveCppGreetUsesGreeter().asLib(), new ObjectiveCppGreeter().asLib());
		delegate = ofNativeLibraryElements((NativeLibraryElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter()));
	}

	private static class ObjectiveCppGreetUsesGreeter extends NativeLibraryElement {
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

		public ObjectiveCppGreetUsesGreeter() {
			header = ofFile(sourceFile("headers", "greet_alice.h", fromResource("objcpp-greeter-lib/greet_alice.h")));
			source = ofFile(sourceFile("objcpp", "greet_alice_impl.mm", fromResource("objcpp-greeter-lib/greet_alice_impl.mm")));
		}
	}
}
