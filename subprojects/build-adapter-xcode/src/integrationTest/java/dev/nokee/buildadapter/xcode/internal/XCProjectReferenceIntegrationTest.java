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

import dev.nokee.platform.xcode.EmptyXCProject;
import dev.nokee.xcode.XCProjectReference;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(TestDirectoryExtension.class)
class XCProjectReferenceIntegrationTest {
	@TestDirectory Path testDirectory;
	XCProjectReference subject;

	@BeforeEach
	void createSubject() {
		new EmptyXCProject("Test").writeToProject(testDirectory);
		subject = XCProjectReference.of(testDirectory.resolve("Test.xcodeproj"));
	}

	@Test
	void hasProjectLocation() {
		assertThat(subject.getLocation(), equalTo(testDirectory.resolve("Test.xcodeproj")));
	}

	@Test
	void hasProjectName() {
		assertThat(subject.getName(), equalTo("Test"));
	}

	@Test
	void canConvertWorkspaceBackToReference() {
		assertThat(subject.load().toReference(), equalTo(XCProjectReference.of(testDirectory.resolve("Test.xcodeproj"))));
	}
}
