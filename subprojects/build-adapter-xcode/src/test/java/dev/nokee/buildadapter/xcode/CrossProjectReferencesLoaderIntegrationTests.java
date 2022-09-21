/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode;

import dev.nokee.samples.xcode.EmptyProject;
import dev.nokee.samples.xcode.GreeterAppWithRemoteLib;
import dev.nokee.xcode.CrossProjectReferencesLoader;
import dev.nokee.xcode.PBXProjectLoader;
import dev.nokee.xcode.XCFileReferencesLoader;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class CrossProjectReferencesLoaderIntegrationTests {
	@TempDir Path testDirectory;
	CrossProjectReferencesLoader subject = new CrossProjectReferencesLoader(new PBXProjectLoader(), new XCFileReferencesLoader(new PBXProjectLoader()));

	@Test
	void returnsEmptyReferencesWhenProjectHasNoProjectReferences() {
		new EmptyProject().writeToProject(testDirectory);

		assertThat(subject.load(XCProjectReference.of(testDirectory.resolve("Empty.xcodeproj"))), emptyIterable());
	}

	@Test
	void returnsProjectReferencesFromProject() {
		new GreeterAppWithRemoteLib().writeToProject(testDirectory);

		assertThat(subject.load(XCProjectReference.of(testDirectory.resolve("GreeterApp.xcodeproj"))),
			contains(XCProjectReference.of(testDirectory.resolve("GreeterLib.xcodeproj"))));
	}
}