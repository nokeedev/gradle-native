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
package dev.nokee.buildadapter.xcode.internal;

import dev.nokee.buildadapter.xcode.internal.plugins.XCWorkspaceLocator;
import dev.nokee.platform.xcode.EmptyXCWorkspace;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(TestDirectoryExtension.class)
class XCWorkspaceLocatorIntegrationTest {
	@TestDirectory Path testDirectory;
	XCWorkspaceLocator subject = new XCWorkspaceLocator();

	@Test
	void returnsEmptyListWhenSearchDirectoryDoesNotExists() {
		assertThat(subject.findWorkspaces(testDirectory.resolve("non-existent")), emptyIterable());
	}

	@Test
	void returnsEmptyListWhenSearchDirectoryDoesNotContainsXcodeWorkspace() {
		assertThat(subject.findWorkspaces(testDirectory), emptyIterable());
	}

	@Test
	void returnsAllXcodeWorkspaceInsideSearchDirectory() {
		new EmptyXCWorkspace("App").writeToProject(testDirectory.toFile());
		new EmptyXCWorkspace("Test").writeToProject(testDirectory.toFile());
		assertThat(subject.findWorkspaces(testDirectory), contains(aFile(withAbsolutePath(endsWith("/App.xcworkspace"))), aFile(withAbsolutePath(endsWith("/Test.xcworkspace")))));
	}

	@Test
	void doesNotReturnNestedXcodeWorkspaceUnderSearchDirectory() {
		new EmptyXCWorkspace("App").writeToProject(testDirectory.toFile());
		new EmptyXCWorkspace("Test").writeToProject(testDirectory.resolve("Test-Projects").toFile());
		assertThat(subject.findWorkspaces(testDirectory), contains(aFile(withAbsolutePath(endsWith("/App.xcworkspace")))));
	}
}
