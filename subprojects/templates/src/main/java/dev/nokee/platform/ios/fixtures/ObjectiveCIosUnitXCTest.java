package dev.nokee.platform.ios.fixtures;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceElements;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.Arrays;
import java.util.List;

public final class ObjectiveCIosUnitXCTest extends SourceElements {
	private final SourceElement main;
	private final SourceElement unitTest = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return ofElements(
				new ObjectiveCUnitTest().withPath("objc/ApplicationTests.m"),
				new NokeeAppUnitXCTestInfoPlist().withPath("resources/Info.plist")
			).getFiles();
		}

		@Override
		public String getSourceSetName() {
			return "unitTest";
		}
	};

    public ObjectiveCIosUnitXCTest(SourceElement main) {
		this.main = main;
	}

	@Override
	public List<SourceElement> getElements() {
		return Arrays.asList(main, unitTest);
	}

	public SourceElement withUiTest() {
		return new ObjectiveCIosUiXCTest(this);
	}

	@SourceFileLocation(file = "ios-objc-app/ApplicationTests/ApplicationTests.m")
	private static class ObjectiveCUnitTest extends RegularFileContent {}

	@SourceFileLocation(file = "ios-objc-app/Info-unitTest.plist")
	public static final class NokeeAppUnitXCTestInfoPlist extends RegularFileContent {}
}
