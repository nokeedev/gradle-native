package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class ObjectiveCppGreeterApp extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*ObjectiveCppGreeter*/> {
	private final NativeSourceElement delegate;

	public ObjectiveCppGreeterApp() {
		super(new ObjectiveCppMainUsesGreeter(), new ObjectiveCppGreeter());
		delegate = ofNativeElements((NativeSourceElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter().asLib()));
	}

	private static class ObjectiveCppMainUsesGreeter extends NativeSourceElement {
		@Override
		public SourceElement getSources() {
			return ofFiles(sourceFile("objcpp", "main.mm", fromResource("objcpp-greeter-app/main.mm")));
		}
	}
}
