package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

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
			return new Header().withPath("headers");
		}

		@SourceFileLocation(file = "objcpp-greeter-lib/src/main/public/greet_alice.h")
		static class Header extends RegularFileContent {}

		@Override
		public SourceFileElement getSource() {
			return new Source().withPath("objcpp");
		}

		@SourceFileLocation(file = "objcpp-greeter-lib/src/main/objcpp/greet_alice_impl.mm")
		static class Source extends RegularFileContent {}
	}
}
