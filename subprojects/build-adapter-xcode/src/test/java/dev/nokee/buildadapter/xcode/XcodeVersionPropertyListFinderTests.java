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
package dev.nokee.buildadapter.xcode;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeVersionPropertyListFinder;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.XcodeTestUtils.createValidXcodeInstallation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class XcodeVersionPropertyListFinderTests {
	FileSystem fileSystem = Jimfs.newFileSystem(Configuration.osX());
	Path developerDir = createValidXcodeInstallation(fileSystem.getPath("/opt/Xcode.app"), "13.4.1").resolve("Contents/Developer");
	XcodeVersionPropertyListFinder subject = new XcodeVersionPropertyListFinder();
	String result = subject.find(developerDir);

	@Test
	void returnsXcodeVersionFromVersionPlistFile() {
		assertThat(result, equalTo("13.4.1"));
	}
}
