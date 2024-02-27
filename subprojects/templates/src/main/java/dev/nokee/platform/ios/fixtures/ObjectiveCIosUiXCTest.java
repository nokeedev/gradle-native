package dev.nokee.platform.ios.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceElements;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.Arrays;
import java.util.List;

public final class ObjectiveCIosUiXCTest extends SourceElements {
	private final SourceElement main;
	private final SourceElement uiTest = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return ofElements(
				new ObjectiveCUiTest().withPath("objc/ApplicationUITests.m"),
				new NokeeAppUiXCTestInfoPlist().withPath("resources/Info.plist")
			).getFiles();
		}

		@Override
		public String getSourceSetName() {
			return "uiTest";
		}
	};

	public ObjectiveCIosUiXCTest(SourceElement main) {
		this.main = main;
	}

	@Override
	public List<SourceElement> getElements() {
		return Arrays.asList(main, uiTest);
	}

	@SourceFileLocation(file = "ios-objc-app/ApplicationUITests/ApplicationUITests.m")
	private static class ObjectiveCUiTest extends RegularFileContent {}

	@SourceFileLocation(file = "ios-objc-app/Info-uiTest.plist")
	public static final class NokeeAppUiXCTestInfoPlist extends RegularFileContent {}
}
