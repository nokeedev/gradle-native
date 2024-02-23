package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SwiftGreeterApp extends GreeterImplementationAwareSourceElement<SourceElement/*SwiftGreeter*/> {
	private final SourceElement delegate;

	public SwiftGreeterApp() {
		super(new SwiftMainUsesGreeter(), new SwiftGreeter());
		delegate = ofElements(getElementUsingGreeter(), getGreeter());
	}

	@Override
	public SwiftMainUsesGreeter getElementUsingGreeter() {
		return (SwiftMainUsesGreeter) super.getElementUsingGreeter();
	}

	public GreeterImplementationAwareSourceElement<SourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter().withImport(capitalize(subprojectPath)), asSubproject(subprojectPath, getGreeter()));
	}

	private static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static class SwiftMainUsesGreeter extends SourceFileElement {
		@Override
		public SourceFile getSourceFile() {
			return sourceFile("swift", "main.swift", fromResource("swift-greeter-app/src/main/swift/main.swift"));
		}

		public SourceFileElement withImport(String moduleToImport) {
			final SourceFile delegate = getSourceFile();
			return new SourceFileElement() {
				@Override
				public SourceFile getSourceFile() {
					return sourceFile(delegate.getPath(), delegate.getName(), Stream.of(
						"import " + moduleToImport,
								"",
								delegate.getContent()
						).collect(Collectors.joining("\n")));
				}
			};
		}
	}
}
