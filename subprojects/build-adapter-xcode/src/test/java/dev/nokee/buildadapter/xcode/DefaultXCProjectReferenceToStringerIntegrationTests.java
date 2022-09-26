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

import dev.nokee.buildadapter.xcode.internal.DefaultXCProjectReferenceToStringer;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DefaultXCProjectReferenceToStringerIntegrationTests {
	@TempDir Path baseDirectory;
	DefaultXCProjectReferenceToStringer subject;

	@BeforeEach
	void createSubject() {
		subject = new DefaultXCProjectReferenceToStringer(baseDirectory);
	}

	@Test
	void returnsToStringForProjectDirectlyInBaseDirectory() {
		val reference = Mockito.mock(XCProjectReference.class);
		Mockito.when(reference.getLocation()).thenReturn(baseDirectory.resolve("Foo.xcodeproj"));

		assertThat(subject.toString(reference), equalTo("Xcode project 'Foo.xcodeproj'"));
	}

	@Test
	void returnsToStringForProjectInSubDirectory() {
		val reference = Mockito.mock(XCProjectReference.class);
		Mockito.when(reference.getLocation()).thenReturn(baseDirectory.resolve("some/dir/Bar.xcodeproj"));

		assertThat(subject.toString(reference), equalTo("Xcode project 'some/dir/Bar.xcodeproj'"));
	}
}
