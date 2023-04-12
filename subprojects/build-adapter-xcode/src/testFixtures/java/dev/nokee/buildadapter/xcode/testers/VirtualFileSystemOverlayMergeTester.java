/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.testers;

import com.google.common.collect.ImmutableList;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayReader;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay.VirtualDirectory.file;
import static dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay.VirtualDirectory.from;
import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.overlayOf;
import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.remappedFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;

public interface VirtualFileSystemOverlayMergeTester {
	Path testDirectory();

	default Path outputFile() {
		return testDirectory().resolve("all-products-headers.yaml");
	}

	default List<Path> inputFiles() {
		return ImmutableList.of(testDirectory().resolve("foo.yaml"), testDirectory().resolve("bar.yaml"));
	}

	default Path derivedDataPath() {
		return testDirectory().resolve("derived-data");
	}

	@BeforeEach
	default void givenOverlayFiles() throws Exception {
		assert inputFiles().size() == 2;
		overlayOf(from("Build/Products/Foo.framework/Headers")
			.remap(file("Foo.h", testDirectory().resolve("Foo/Foo.h")))
			.remap(file("MyFoo.h", testDirectory().resolve("Foo/MyFoo.h"))).build())
			.writeTo(inputFiles().get(0));
		overlayOf(from("Build/Products/Bar.framework/Headers")
			.remap(file("Bar.h", testDirectory().resolve("Bar/Bar.h"))).build())
			.writeTo(inputFiles().get(1));
	}

	@Test
	default void mergesOverlays() throws IOException {
		try (val reader = new VirtualFileSystemOverlayReader(Files.newBufferedReader(outputFile()))) {
			val result = reader.read();
			assertThat(result, contains(
				allOf(named(endsWith("Build/Products/Foo.framework/Headers")),
					containsInAnyOrder(
						remappedFile("Foo.h", withAbsolutePath(endsWith("/Foo/Foo.h"))),
						remappedFile("MyFoo.h", withAbsolutePath(endsWith("/Foo/MyFoo.h")))
					)),
				allOf(named(endsWith("Build/Products/Bar.framework/Headers")),
					containsInAnyOrder(
						remappedFile("Bar.h", withAbsolutePath(endsWith("/Bar/Bar.h")))
					))
			));
		}
	}

	@Test
	default void rebaseVirtualDirectoriesToCurrentDerivedDataPath() throws IOException {
		try (val reader = new VirtualFileSystemOverlayReader(Files.newBufferedReader(outputFile()))) {
			val result = reader.read();
			assertThat(result, contains(
				named(derivedDataPath().resolve("Build/Products/Foo.framework/Headers").toString()),
				named(derivedDataPath().resolve("Build/Products/Bar.framework/Headers").toString())
			));
		}
	}
}
