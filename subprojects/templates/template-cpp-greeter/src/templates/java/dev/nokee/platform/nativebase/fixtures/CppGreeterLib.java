package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

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
			return ofFile(sourceFile("headers", "greet_alice.h", fromResource("cpp-greeter-lib/greet_alice.h")));
		}

		@Override
		public SourceFileElement getSource() {
			return ofFile(sourceFile("cpp", "greet_alice.cpp", fromResource("cpp-greeter-lib/greet_alice.cpp")));
		}
	}
}
