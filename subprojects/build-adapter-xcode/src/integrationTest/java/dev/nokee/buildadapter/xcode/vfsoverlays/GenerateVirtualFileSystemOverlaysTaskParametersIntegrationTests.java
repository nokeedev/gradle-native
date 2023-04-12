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
package dev.nokee.buildadapter.xcode.vfsoverlays;

import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask.Parameters;
import dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils;
import dev.nokee.utils.FileSystemLocationUtils;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

@ExtendWith(TestDirectoryExtension.class)
class GenerateVirtualFileSystemOverlaysTaskParametersIntegrationTests {
	@TestDirectory Path testDirectory;

	@Nested
	class CheckCopyTo {
		Parameters src = objectFactory().newInstance(Parameters.class);
		Parameters dst = objectFactory().newInstance(Parameters.class);

		@BeforeEach
		void givenCopiedParameters() {
			src.getOverlays().create("my/path/to/dir", it -> it.getEntries().create("foo.h", e -> e.getLocation().set("/path/to/foo.h")));
			src.getOutputFile().set(testDirectory.resolve("out.yaml").toFile());

			src.copyTo(dst);
		}

		@Test
		void copiesOutputFile() {
			assertThat(dst.getOutputFile().map(FileSystemLocationUtils::asPath),
				providerOf(testDirectory.resolve("out.yaml")));
		}

		@Test
		void copiesOverlays() {
			assertThat(dst.getOverlays(), contains(allOf(
				named("my/path/to/dir"),
				VirtualFileSystemOverlayTestUtils.entries(contains(allOf(
					named("foo.h"),
					VirtualFileSystemOverlayTestUtils.location(providerOf("/path/to/foo.h"))
				))))));
		}
	}
}
