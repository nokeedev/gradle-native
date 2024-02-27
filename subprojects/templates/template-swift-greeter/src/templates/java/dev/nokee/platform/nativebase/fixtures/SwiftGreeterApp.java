package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter;

import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

public final class SwiftGreeterApp extends GreeterImplementationAwareSourceElement {
	@Override
	public SwiftSourceFileElement getElementUsingGreeter() {
		return new SwiftMainUsesGreeter();
	}

	@Override
	public SourceElement getGreeter() {
		return new SwiftGreeter();
	}

	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter().withImport(capitalize(subprojectPath)), getGreeter().as(subproject(subprojectPath)));
	}

	private static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static class SwiftMainUsesGreeter extends SwiftSourceFileElement {
		@Override
		public SourceFile getSourceFile() {
			return new Source().withPath("swift/main.swift").getSourceFile();
		}

		@SourceFileLocation(file = "swift-greeter-app/src/main/swift/main.swift")
		static class Source extends RegularFileContent {}
	}
}
