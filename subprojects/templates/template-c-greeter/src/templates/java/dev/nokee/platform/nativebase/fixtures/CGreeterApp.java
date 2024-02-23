package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.CGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CGreeterApp extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*CGreeter*/> {
	private final NativeSourceElement delegate;

	public CGreeterApp() {
		super(new CMainUsesGreeter(), new CGreeter());
		delegate = ofNativeElements((NativeSourceElement) getElementUsingGreeter(), getGreeter());
	}

	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectName) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectName, getGreeter().asLib()));
	}

	public SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CGreeterTest());
	}

	private static class CMainUsesGreeter extends NativeSourceElement {
		@Override
		public SourceElement getSources() {
			return ofFile(sourceFile("c", "main.c", fromResource("c-greeter-app/main.c")));
		}
	}
}
