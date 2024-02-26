package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new ObjectiveCGreetUsesGreeter().asLib();
	}

	@Override
	public SourceElement getGreeter() {
		return new ObjectiveCGreeter().asLib();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(subproject(subprojectPath)));
	}

	private static class ObjectiveCGreetUsesGreeter extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return ofFile(sourceFile("headers", "greet_alice.h", fromResource(Header.class)));
		}

		@SourceFileLocation(file = "objc-greeter-lib/src/main/public/greet_alice.h")
		interface Header {}

		@Override
		public SourceFileElement getSource() {
			return ofFile(sourceFile("objc", "greet_alice_impl.m", fromResource(Source.class)));
		}

		@SourceFileLocation(file = "objc-greeter-lib/src/main/objc/greet_alice_impl.m")
		interface Source {}
	}
}
