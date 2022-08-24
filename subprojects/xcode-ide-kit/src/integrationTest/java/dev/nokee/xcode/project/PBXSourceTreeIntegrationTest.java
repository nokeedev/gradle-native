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
package dev.nokee.xcode.project;

import com.google.common.collect.Streams;
import dev.nokee.samples.xcode.CommonSourceTree;
import dev.nokee.samples.xcode.CustomSourceTree;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@ExtendWith(TestDirectoryExtension.class)
class PBXSourceTreeIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void canReadProjectWithCustomSourceTree() throws IOException {
		new CustomSourceTree().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("CustomSourceTree.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("main.c"), sourceTree(PBXSourceTree.of("MY_SOURCE_ROOT")))));
		}
	}

	@Test
	void canReadProjectWithStandardSourceTree() throws IOException {
		new CommonSourceTree().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("CommonSourceTree.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("/path/to/my/absolute-file"), sourceTree(PBXSourceTree.ABSOLUTE))));
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("relative-to-built-products"), sourceTree(PBXSourceTree.BUILT_PRODUCTS_DIR))));
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("relative-to-developer-directory"), sourceTree(PBXSourceTree.DEVELOPER_DIR))));
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("relative-to-sdk"), sourceTree(PBXSourceTree.SDKROOT))));
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("CommonSourceTree/relative-to-project"), sourceTree(PBXSourceTree.SOURCE_ROOT))));
			assertThat(allFileReferences(project.getMainGroup()),
				hasItem(allOf(pathOf("relative-to-group"), sourceTree(PBXSourceTree.GROUP))));
		}
	}

	private static Iterable<PBXFileReference> allFileReferences(PBXGroup group) {
		return group.getChildren().stream().flatMap(it -> {
			if (it instanceof PBXGroup) {
				return Streams.stream(allFileReferences((PBXGroup) it));
			} else if (it instanceof PBXFileReference) {
				return Stream.of((PBXFileReference) it);
			} else {
				return Stream.of();
			}
		}).collect(toList());
	}

	private static Matcher<PBXFileReference> pathOf(String path) {
		return new FeatureMatcher<PBXFileReference, String>(equalTo(path), "", "") {
			@Override
			protected String featureValueOf(PBXFileReference fileReference) {
				return fileReference.getPath().orElse(null);
			}
		};
	}

	private static Matcher<PBXFileReference> sourceTree(PBXSourceTree sourceTree) {
		return new FeatureMatcher<PBXFileReference, PBXSourceTree>(equalTo(sourceTree), "", "") {
			@Override
			protected PBXSourceTree featureValueOf(PBXFileReference fileReference) {
				return fileReference.getSourceTree();
			}
		};
	}
}
