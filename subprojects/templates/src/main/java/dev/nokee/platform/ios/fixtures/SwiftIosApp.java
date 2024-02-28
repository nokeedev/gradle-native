/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nokee.platform.ios.fixtures;

import dev.gradleplugins.fixtures.sources.DirectoryContent;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.List;

public final class SwiftIosApp extends SourceElement {
	private final SourceElement main = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return ofElements(
				new SwiftAppDelegate().withPath("swift"),
				new SwiftSceneDelegate().withPath("swift"),
				new SwiftViewController().withPath("swift"),
				new NokeeAppBaseLanguage().withPath("resources/Base.lproj"),
				new NokeeAppInfoPlist().withPath("resources"),
				new NokeeAppAssets().withPath("resources/Assets.xcassets")
			).getFiles();
		}
	};

	@Override
	public List<SourceFile> getFiles() {
		return main.getFiles();
	}

	public SwiftIosUnitXCTest withUnitTest() {
		return new SwiftIosUnitXCTest(this);
	}

	@SourceFileLocation(file = "ios-swift-app/Application/Info.plist")
	private static class NokeeAppInfoPlist extends RegularFileContent {}

	@SourceFileLocation(file = "ios-swift-app/Application/Base.lproj")
	private static class NokeeAppBaseLanguage extends DirectoryContent {}

	@SourceFileLocation(file = "ios-swift-app/Application/Assets.xcassets")
	private static class NokeeAppAssets extends DirectoryContent {}

	@SourceFileLocation(file = "ios-swift-app/Application/AppDelegate.swift")
	private static class SwiftAppDelegate extends RegularFileContent {}

	@SourceFileLocation(file = "ios-swift-app/Application/SceneDelegate.swift")
	private static class SwiftSceneDelegate extends RegularFileContent {}

	@SourceFileLocation(file = "ios-swift-app/Application/ViewController.swift")
	private static class SwiftViewController extends RegularFileContent {}
}
