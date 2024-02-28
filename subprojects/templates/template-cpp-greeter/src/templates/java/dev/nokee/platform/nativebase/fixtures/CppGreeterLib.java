package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.CppFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

public final class CppGreeterLib extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new CppGreetUsingGreeter().asLib();
	}

	@Override
	public SourceElement getGreeter() {
		return new CppGreeter().asLib();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(subproject(subprojectPath)));
	}

	public SourceElement withGenericTestSuite() {
		return ofElements(getElementUsingGreeter(), getGreeter(), new CppGreeterTest());
	}

	private static class CppGreetUsingGreeter extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return new Header();
		}

		@SourceFileLocation(file = "cpp-greeter-lib/src/main/public/greet_alice.h")
		static class Header extends NativeHeaderFileElement {}

		@Override
		public SourceFileElement getSource() {
			return new Source();
		}

		@SourceFileLocation(file = "cpp-greeter-lib/src/main/cpp/greet_alice.cpp")
		static class Source extends CppFileElement {}
	}
}
