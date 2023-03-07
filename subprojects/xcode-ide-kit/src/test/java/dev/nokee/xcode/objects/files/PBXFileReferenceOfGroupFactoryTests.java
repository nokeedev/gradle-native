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
package dev.nokee.xcode.objects.files;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PBXFileReferenceOfGroupFactoryTests {
	@Test
	void canCreateGroupFileReferenceOfPathToFileWithExtension() {
		assertThat(ofGroup("file.txt"),
			equalTo(PBXFileReference.builder().name("file").path("file.txt").sourceTree(GROUP).build()));
	}

	@Test
	void canCreateGroupFileReferenceOfPathToChildFileWithExtension() {
		assertThat(ofGroup("test/file.txt"),
			equalTo(PBXFileReference.builder().name("file").path("test/file.txt").sourceTree(GROUP).build()));
	}

	@Test
	void canCreateGroupFileReferenceOfPathToDirectory() {
		assertThat(ofGroup("dir"),
			equalTo(PBXFileReference.builder().name("dir").path("dir").sourceTree(GROUP).build()));
	}

	@Test
	void canCreateGroupFileReferenceOfPathToDirectoryWithTailingPathSeparator() {
		assertThat(ofGroup("dir/"),
			equalTo(PBXFileReference.builder().name("dir").path("dir").sourceTree(GROUP).build()));
	}

	@Test
	void throwsExceptionWhenPathIsEmpty() {
		assertAll(
			() -> assertThrows(IllegalArgumentException.class, () -> ofGroup("")),
			() -> assertThrows(IllegalArgumentException.class, () -> ofGroup("  "))
		);
	}

	@Test
	void normalizePathSeparator() {
		assertThat(ofGroup("my\\path\\to\\file.txt").getPath(),
			optionalWithValue(equalTo("my/path/to/file.txt")));
	}
}
