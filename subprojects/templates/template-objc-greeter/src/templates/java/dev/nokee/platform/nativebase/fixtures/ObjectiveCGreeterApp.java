package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

public final class ObjectiveCGreeterApp extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new ObjectiveCMainUsesGreeter().withPath("objc");
	}

	@Override
	public SourceElement getGreeter() {
		return new ObjectiveCGreeter();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(lib()).as(subproject(subprojectPath)));
	}

	@SourceFileLocation(file = "objc-greeter-app/src/main/objc/main.m")
	private static class ObjectiveCMainUsesGreeter extends RegularFileContent {}
}
