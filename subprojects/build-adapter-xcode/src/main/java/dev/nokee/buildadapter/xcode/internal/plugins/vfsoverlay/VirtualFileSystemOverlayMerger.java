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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import com.google.common.collect.ImmutableList;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class VirtualFileSystemOverlayMerger {
	private final List<Path> files = new ArrayList<>();
	private Path rebaseLocation;

	public VirtualFileSystemOverlayMerger withOverlayFile(Path file) {
		files.add(file);
		return this;
	}

	public VirtualFileSystemOverlayMerger rebaseOn(Path path) {
		rebaseLocation = path;
		return this;
	}

	public void mergeTo(Path outputFile) throws IOException {
		// Here were merge all vfsoverlay files together because -ivfsoverlay (clang) can only appear once as opposed to -vfsoverlay (swiftc)
		try (val writer = new VirtualFileSystemOverlayWriter(Files.newBufferedWriter(outputFile))) {
			val directories = ImmutableList.<VirtualFileSystemOverlay.VirtualDirectory>builder();
			for (val overlayFile : files) {
				try (val reader = new VirtualFileSystemOverlayReader(Files.newBufferedReader(overlayFile))) {
					val overlay = reader.read();
					overlay.forEach(it -> {
						// We know that *our* vfsoverlay are relative to the derived data path,
						//   so we need to resolve the virtual directory against the current derived data path.
						directories.add(new VirtualFileSystemOverlay.VirtualDirectory(rebaseLocation.resolve(it.getName()).toString(), ImmutableList.copyOf(it)));
					});
				}
			}

			writer.write(new VirtualFileSystemOverlay(directories.build()));
		}
	}
}
