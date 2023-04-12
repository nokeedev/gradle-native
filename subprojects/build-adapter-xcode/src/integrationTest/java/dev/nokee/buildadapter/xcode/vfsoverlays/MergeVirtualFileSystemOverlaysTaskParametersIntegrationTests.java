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

import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.MergeVirtualFileSystemOverlaysTask.Parameters;
import dev.nokee.utils.FileSystemLocationUtils;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(TestDirectoryExtension.class)
class MergeVirtualFileSystemOverlaysTaskParametersIntegrationTests {
	@TestDirectory Path testDirectory;

	@Nested
	class CheckCopyTo {
		Parameters src = objectFactory().newInstance(Parameters.class);
		Parameters dst = objectFactory().newInstance(Parameters.class);

		@BeforeEach
		void givenCopiedParameters() {
			src.getSources().setFrom(testDirectory.resolve("first-overlay.yaml"), testDirectory.resolve("second-overlay.yaml"));
			src.getOutputFile().set(testDirectory.resolve("out.yaml").toFile());
			src.getDerivedDataPath().set(testDirectory.resolve("derived-data").toFile());

			src.copyTo(dst);
		}

		@Test
		void copiesOutputFile() {
			assertThat(dst.getOutputFile().map(FileSystemLocationUtils::asPath),
				providerOf(testDirectory.resolve("out.yaml")));
		}

		@Test
		void copiesSources() {
			assertThat(dst.getSources(), containsInAnyOrder(aFile(withAbsolutePath(endsWith("/first-overlay.yaml"))),
				aFile(withAbsolutePath(endsWith("/second-overlay.yaml")))));
		}

		@Test
		void copiesDerivedDataPath() {
			assertThat(dst.getDerivedDataPath().map(FileSystemLocationUtils::asPath),
				providerOf(testDirectory.resolve("derived-data")));
		}
	}
}
