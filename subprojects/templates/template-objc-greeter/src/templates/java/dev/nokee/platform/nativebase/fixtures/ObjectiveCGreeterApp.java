package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class ObjectiveCGreeterApp extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*ObjectiveCGreeter*/> {
	private final NativeSourceElement delegate;

	public ObjectiveCGreeterApp() {
		super(new ObjectiveCMainUsesGreeter(), new ObjectiveCGreeter());
		delegate = ofNativeElements((NativeSourceElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter().asLib()));
	}

	private static class ObjectiveCMainUsesGreeter extends NativeSourceElement {
		@Override
		public SourceElement getSources() {
			return ofFiles(sourceFile("objc", "main.m", fromResource("objc-greeter-app/main.m")));
		}
	}
}
