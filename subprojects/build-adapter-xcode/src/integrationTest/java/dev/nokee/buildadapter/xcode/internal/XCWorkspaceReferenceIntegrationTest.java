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

import dev.nokee.platform.xcode.EmptyXCWorkspace;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(TestDirectoryExtension.class)
class XCWorkspaceReferenceIntegrationTest {
	@TestDirectory Path testDirectory;
	XCWorkspaceReference subject;

	@BeforeEach
	void createSubject() {
		new EmptyXCWorkspace("Test").writeToProject(testDirectory);
		subject = XCWorkspaceReference.of(testDirectory.resolve("Test.xcworkspace"));
	}

	@Test
	void hasWorkspaceLocation() {
		assertThat(subject.getLocation(), equalTo(testDirectory.resolve("Test.xcworkspace")));
	}

	@Test
	void canConvertWorkspaceBackToReference() {
		assertThat(subject.load().toReference(), equalTo(XCWorkspaceReference.of(testDirectory.resolve("Test.xcworkspace"))));
	}
}
