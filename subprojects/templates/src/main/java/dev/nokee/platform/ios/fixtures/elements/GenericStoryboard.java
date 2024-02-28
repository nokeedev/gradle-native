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

package dev.nokee.platform.ios.fixtures.elements;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

/**
 * Artificial storyboard derived from Xcode starter sample's LaunchScreen.storyboard.
 * It's only meant for building, not running.
 */
public final class GenericStoryboard extends SourceFileElement {
	private final SourceFile source;

	public GenericStoryboard(String name) {
		this.source = new Content().withPath("resources/Base.lproj", name + ".storyboard").getSourceFile();

	}
	@Override
	public SourceFile getSourceFile() {
		return source;
	}

	@SourceFileLocation(file = "ios-generic-storyboard/Generic.storyboard")
	static class Content extends RegularFileContent {}
}
