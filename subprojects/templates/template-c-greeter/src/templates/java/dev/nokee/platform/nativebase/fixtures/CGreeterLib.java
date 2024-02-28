package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.nativebase.CFileElement;
import dev.gradleplugins.fixtures.sources.nativebase.NativeHeaderFileElement;
import dev.nokee.platform.jni.fixtures.CGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

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
			return new Header();
		}

		@SourceFileLocation(file = "c-greeter-lib/src/main/public/greet_alice.h")
		static class Header extends NativeHeaderFileElement {}

		@Override
		public SourceFileElement getSource() {
			return new Source();
		}

		@SourceFileLocation(file = "c-greeter-lib/src/main/c/greet_alice.c")
		static class Source extends CFileElement {}
	}
}
