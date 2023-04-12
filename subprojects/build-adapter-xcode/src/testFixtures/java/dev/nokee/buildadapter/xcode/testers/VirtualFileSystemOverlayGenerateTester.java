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
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.overlayAt;
import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.remappedFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;

public interface VirtualFileSystemOverlayGenerateTester {
	Path testDirectory();

	default Path outputFile() {
		return testDirectory().resolve("out.yaml");
	}

	default VirtualFileSystemOverlay overlay() {
		return new VirtualFileSystemOverlay(ImmutableList.of(new VirtualFileSystemOverlay.VirtualDirectory("Build/Products/MyFramework.framework/Headers", ImmutableList.of(new VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry("MyFramework.h", testDirectory().resolve("MyFramework/MyFramework.h").toString()), new VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry("MyPublicHeader.h", testDirectory().resolve("MyFramework/MyPublicHeader.h").toString())))));
	}

	@Test
	default void canGenerateOverlayFile() throws IOException {
		val result = overlayAt(testDirectory().resolve("out.yaml"));
		assertThat(result, contains(allOf(
			named("Build/Products/MyFramework.framework/Headers"),
			containsInAnyOrder(
				remappedFile("MyFramework.h", withAbsolutePath(endsWith("/MyFramework/MyFramework.h"))),
				remappedFile("MyPublicHeader.h", withAbsolutePath(endsWith("/MyFramework/MyPublicHeader.h")))
			))));
	}
}
