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
import dev.gradleplugins.fixtures.sources.NativeSourceFileElement;
import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.List;

public final class ObjectiveCIosApp extends SourceElement {
	private final SourceElement main = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return ofElements(
				new ObjectiveCAppDelegate(), new ObjectiveCSceneDelegate(), new ObjectiveCViewController(),
				new ObjectiveCMain().withPath("objc/main.m"),
				new NokeeAppBaseLanguage().withPath("resources/Base.lproj"),
				new NokeeAppInfoPlist().withPath("resources/Info.plist"),
				new NokeeAppAssets().withPath("resources/Assets.xcassets")
			).getFiles();
		}
	};

	@Override
	public List<SourceFile> getFiles() {
		return main.getFiles();
	}

	public ObjectiveCIosUnitXCTest withUnitTest() {
		return new ObjectiveCIosUnitXCTest(this);
	}

	public SourceElement baseLang() {
		return new NokeeAppBaseLanguage().withPath("resources/Base.lproj");
	}

	@SourceFileLocation(file = "ios-objc-app/Application/Info.plist")
	private static class NokeeAppInfoPlist extends RegularFileContent {}

	@SourceFileLocation(file = "ios-objc-app/Application/Base.lproj")
	private static class NokeeAppBaseLanguage extends DirectoryContent {}

	@SourceFileLocation(file = "ios-objc-app/Application/Assets.xcassets")
	private static class NokeeAppAssets extends DirectoryContent {}

	private static class ObjectiveCAppDelegate extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return new Header().withPath("headers/AppDelegate.h");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/AppDelegate.h")
		static class Header extends RegularFileContent {
		}

		@Override
		public SourceFileElement getSource() {
			return new Source().withPath("objc/AppDelegate.m");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/AppDelegate.m")
		static class Source extends RegularFileContent {
		}
	}

	private static class ObjectiveCSceneDelegate extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return new Header().withPath("headers/SceneDelegate.h");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/SceneDelegate.h")
		static class Header extends RegularFileContent {
		}

		@Override
		public SourceFileElement getSource() {
			return new Source().withPath("objc/SceneDelegate.m");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/SceneDelegate.m")
		static class Source extends RegularFileContent {
		}
	}

	private static class ObjectiveCViewController extends NativeSourceFileElement {
		@Override
		public SourceFileElement getHeader() {
			return new Header().withPath("headers/ViewController.h");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/ViewController.h")
		static class Header extends RegularFileContent {
		}

		@Override
		public SourceFileElement getSource() {
			return new Source().withPath("objc/ViewController.m");
		}

		@SourceFileLocation(file = "ios-objc-app/Application/ViewController.m")
		static class Source extends RegularFileContent {
		}
	}

	@SourceFileLocation(file = "ios-objc-app/Application/main.m")
	private static class ObjectiveCMain extends RegularFileContent {
	}
}
