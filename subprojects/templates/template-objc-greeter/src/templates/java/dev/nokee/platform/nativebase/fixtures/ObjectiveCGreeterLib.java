package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

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
			return new Header().withPath("headers/greet_alice.h");
		}

		@SourceFileLocation(file = "objc-greeter-lib/src/main/public/greet_alice.h")
		static class Header extends RegularFileContent {}

		@Override
		public SourceFileElement getSource() {
			return new Source().withPath("objc/greet_alice_impl.m");
		}

		@SourceFileLocation(file = "objc-greeter-lib/src/main/objc/greet_alice_impl.m")
		static class Source extends RegularFileContent {}
	}
}
