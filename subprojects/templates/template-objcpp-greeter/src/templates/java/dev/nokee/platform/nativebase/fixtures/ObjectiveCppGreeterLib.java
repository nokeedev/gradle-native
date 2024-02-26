package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCppGreeterLib extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new ObjectiveCppGreetUsesGreeter().asLib();
	}

	@Override
	public SourceElement getGreeter() {
		return new ObjectiveCppGreeter().asLib();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(subproject(subprojectPath)));
	}

	private static class ObjectiveCppGreetUsesGreeter extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return ofFile(sourceFile("headers", "greet_alice.h", fromResource(Header.class)));
		}

		@SourceFileLocation(file = "objcpp-greeter-lib/src/main/public/greet_alice.h")
		interface Header {}

		@Override
		public SourceFileElement getSource() {
			return ofFile(sourceFile("objcpp", "greet_alice_impl.mm", fromResource(Source.class)));
		}

		@SourceFileLocation(file = "objcpp-greeter-lib/src/main/objcpp/greet_alice_impl.mm")
		interface Source {}
	}
}
