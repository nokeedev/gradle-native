package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SwiftSourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;

public final class SwiftGreeterTest extends SwiftSourceFileElement {
	private final SourceFile source;

	public SwiftGreeterTest(String testedModuleName) {
		this.source = new Source().withTestedModuleName(testedModuleName)
			.withPath("swift/greeter_test.swift").getSourceFile();
	}

	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "swift-greeter-test/src/main/swift/greeter_test.swift", properties = {
		@SourceFileProperty(regex = "^import (SwiftGreeter)$", name = "testedModuleName")
	})
	static class Source extends RegularFileContent {
		public Source withTestedModuleName(String s) {
			properties.put("testedModuleName", s);
			return this;
		}
	}
}
