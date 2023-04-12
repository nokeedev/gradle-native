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
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayMerger;
import dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayMergeTester;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

class VirtualFileSystemOverlayMergerTests implements VirtualFileSystemOverlayMergeTester {
	FileSystem fileSystem = Jimfs.newFileSystem(Configuration.osX());
	Path testDirectory = fileSystem.getPath("test");

	@BeforeEach
	void exec() throws IOException {
		VirtualFileSystemOverlayMerger subject = new VirtualFileSystemOverlayMerger();
		inputFiles().forEach(subject::withOverlayFile);
		subject.rebaseOn(derivedDataPath());
		subject.mergeTo(outputFile());
	}

	@Override
	public Path testDirectory() {
		try {
			return Files.createDirectories(testDirectory);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
