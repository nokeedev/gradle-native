package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

public final class ObjectiveCppGreeterApp extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new ObjectiveCppMainUsesGreeter();
	}

	@Override
	public SourceElement getGreeter() {
		return new ObjectiveCppGreeter();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(lib()).as(subproject(subprojectPath)));
	}

	private static class ObjectiveCppMainUsesGreeter extends SourceFileElement {
		@Override
		@SourceFileLocation(file = "objcpp-greeter-app/src/main/objcpp/main.mm")
		public SourceFile getSourceFile() {
			return sourceFile("objcpp", "main.mm", fromResource("objcpp-greeter-app/main.mm"));
		}
	}
}
