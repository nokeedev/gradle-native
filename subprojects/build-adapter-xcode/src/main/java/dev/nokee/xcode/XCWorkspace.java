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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.workspace.XCWorkspaceData;
import dev.nokee.xcode.workspace.XCWorkspaceDataReader;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FilenameUtils.removeExtension;

@EqualsAndHashCode
public final class XCWorkspace implements Serializable {
	public static XCWorkspace open(Path workspaceLocation) {
		return new XCWorkspace(workspaceLocation);
	}

	private final File location;
	private final List<XCProjectReference> projects;
	private final List<String> schemeNames;

	// friends with XCWorkspaceReference
	XCWorkspace(Path workspaceLocation) {
		assert Files.exists(workspaceLocation) && Files.isDirectory(workspaceLocation) : "invalid workspace";

		this.location = workspaceLocation.toFile();
		val layout = new XCWorkspaceLayout(workspaceLocation);
		try {
			val data = new XCWorkspaceDataReader(Files.newBufferedReader(layout.getContentFile(), UTF_8)).read();
			val resolver = new XCFileReferenceResolver(layout.getBaseDirectory().toFile());
			projects = data.getFileRefs().stream().map(resolver::resolve).map(File::toPath).map(XCProjectReference::of).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		schemeNames = projects.stream().map(it -> it.getLocation().resolve("xcshareddata/xcschemes")).filter(Files::isDirectory).flatMap(it -> {
			val builder = ImmutableList.<String>builder();
			try (final DirectoryStream<Path> xcodeSchemeStream = Files.newDirectoryStream(it, "*.xcscheme")) {
				for (Path xcodeSchemeFile : xcodeSchemeStream) {
					builder.add(removeExtension(xcodeSchemeFile.getFileName().toString()));
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return builder.build().stream();
		}).distinct().collect(ImmutableList.toImmutableList());
	}

	public Path getLocation() {
		return location.toPath();
	}

	public List<XCProjectReference> getProjectLocations() {
		return projects;
	}

	public List<String> getSchemeNames() {
		return schemeNames;
	}
}
