package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SwiftGreeterLib extends GreeterImplementationAwareSourceElement<SourceElement/*SwiftGreeter*/> {
	private final SourceElement delegate;

	public SwiftGreeterLib() {
		super(new SwiftGreetUsesGreeter(), new SwiftGreeter());
		delegate = ofElements(getElementUsingGreeter(), getGreeter());
	}

	@Override
	public SwiftGreetUsesGreeter getElementUsingGreeter() {
		return (SwiftGreetUsesGreeter) super.getElementUsingGreeter();
	}

	public GreeterImplementationAwareSourceElement<SourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter().withImport(capitalize(subprojectPath)), asSubproject(subprojectPath, getGreeter()));
	}

	private static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static class SwiftGreetUsesGreeter extends SourceFileElement {
		@Override
		public SourceFile getSourceFile() {
			return sourceFile("swift", "greeter.swift", fromResource("swift-greeter-lib/src/main/swift/greeter.swift"));
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
