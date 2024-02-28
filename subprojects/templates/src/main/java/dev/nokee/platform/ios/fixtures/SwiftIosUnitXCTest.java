package dev.nokee.platform.ios.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceElements;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.Arrays;
import java.util.List;

public final class SwiftIosUnitXCTest extends SourceElements {
	private final SourceElement main;
	private final SourceElement unitTest = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return ofElements(
				new SwiftUnitTest().withPath("swift"),
				new NokeeAppUnitXCTestInfoPlist().withPath("resources", "Info.plist")
			).getFiles();
		}

		@Override
		public String getSourceSetName() {
			return "unitTest";
		}
	};

    public SwiftIosUnitXCTest(SourceElement main) {
		this.main = main;
	}

	@Override
	public List<SourceElement> getElements() {
		return Arrays.asList(main, unitTest);
	}

	public SourceElement withUiTest() {
		return new SwiftIosUiXCTest(this);
	}

	@SourceFileLocation(file = "ios-swift-app/ApplicationTests/ApplicationTests.swift")
	private static class SwiftUnitTest extends RegularFileContent {}

	@SourceFileLocation(file = "ios-swift-app/Info-unitTest.plist")
	public static final class NokeeAppUnitXCTestInfoPlist extends RegularFileContent {}
}
