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
import static dev.nokee.xcode.objects.files.PBXFileReference.ofBuiltProductsDir;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PBXFileReferenceOfBuiltProductDirFactoryTests {
	@Test
	void canCreateBuiltProductsFileReferenceOfPathToFileWithExtension() {
		assertThat(ofBuiltProductsDir("file.txt"),
			equalTo(PBXFileReference.builder().name("file").path("file.txt").sourceTree(BUILT_PRODUCTS_DIR).build()));
	}

	@Test
	void canCreateBuiltProductsFileReferenceOfPathToChildFileWithExtension() {
		assertThat(ofBuiltProductsDir("test/file.txt"),
			equalTo(PBXFileReference.builder().name("file").path("test/file.txt").sourceTree(BUILT_PRODUCTS_DIR).build()));
	}

	@Test
	void canCreateBuiltProductsFileReferenceOfPathToDirectory() {
		assertThat(ofBuiltProductsDir("dir"),
			equalTo(PBXFileReference.builder().name("dir").path("dir").sourceTree(BUILT_PRODUCTS_DIR).build()));
	}

	@Test
	void canCreateBuiltProductsFileReferenceOfPathToDirectoryWithTailingPathSeparator() {
		assertThat(ofBuiltProductsDir("dir/"),
			equalTo(PBXFileReference.builder().name("dir").path("dir").sourceTree(BUILT_PRODUCTS_DIR).build()));
	}

	@Test
	void throwsExceptionWhenPathIsEmpty() {
		assertAll(
			() -> assertThrows(IllegalArgumentException.class, () -> ofBuiltProductsDir("")),
			() -> assertThrows(IllegalArgumentException.class, () -> ofBuiltProductsDir("  "))
		);
	}

	@Test
	void normalizePathSeparator() {
		assertThat(ofBuiltProductsDir("my\\path\\to\\file.txt").getPath(),
			optionalWithValue(equalTo("my/path/to/file.txt")));
	}
}
