package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SwiftGreeterTest extends SourceFileElement {
	private final String testedModuleName;

	public SwiftGreeterTest(String testedModuleName) {
		this.testedModuleName = testedModuleName;
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFile getSourceFile() {
		return sourceFile("swift", "greeter_test.swift", fromResource("swift-greeter-test/greeter_test.swift").replace("import SwiftGreeter", "import " + testedModuleName));
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
