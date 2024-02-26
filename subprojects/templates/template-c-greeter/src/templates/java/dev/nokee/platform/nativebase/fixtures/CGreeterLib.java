package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.CGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class CGreeterLib extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new CGreetUsingGreeter().asLib();
	}

	@Override
	public SourceElement getGreeter() {
		return new CGreeter().asLib();
	}

	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(subproject(subprojectPath)));
	}

	public SourceElement withGenericTestSuite() {
		return ofElements(getElementUsingGreeter(), getGreeter(), new CGreeterTest());
	}

	private static class CGreetUsingGreeter extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return ofFile(sourceFile("headers", "greet_alice.h", fromResource(Header.class)));
		}

		@SourceFileLocation(file = "c-greeter-lib/src/main/public/greet_alice.h")
		interface Header {}

		@Override
		public SourceFileElement getSource() {
			return ofFile(sourceFile("c", "greet_alice.c", fromResource(Source.class)));
		}

		@SourceFileLocation(file = "c-greeter-lib/src/main/c/greet_alice.c")
		interface Source {}
	}
}
