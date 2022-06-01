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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.workspace.XCWorkspaceDataReader;
import lombok.EqualsAndHashCode;
import lombok.val;

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
public final class XCWorkspaceReference implements Serializable {
	private final File location;

	private XCWorkspaceReference(Path location) {
		this.location = location.toFile();
	}

	public Path getLocation() {
		return location.toPath();
	}

	public XCWorkspace load() {
		val layout = new XCWorkspaceLayout(getLocation());

		List<XCProjectReference> projects = null;
		try {
			val data = new XCWorkspaceDataReader(Files.newBufferedReader(layout.getContentFile(), UTF_8)).read();
			val resolver = new XCFileReferenceResolver(layout.getBaseDirectory().toFile());
			projects = data.getFileRefs().stream().map(resolver::resolve).map(File::toPath).map(XCProjectReference::of).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		// TODO: Add support for implicit scheme: xcodebuild -list -workspace `getLocation()` -json
		val schemeNames = projects.stream().map(it -> it.getLocation().resolve("xcshareddata/xcschemes")).filter(Files::isDirectory).flatMap(it -> {
			val builder = ImmutableList.<String>builder();
			try (final DirectoryStream<Path> xcodeSchemeStream = Files.newDirectoryStream(it, "*.xcscheme")) {
				for (Path xcodeSchemeFile : xcodeSchemeStream) {
					builder.add(removeExtension(xcodeSchemeFile.getFileName().toString()));
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return builder.build().stream();
		}).distinct().collect(ImmutableSet.toImmutableSet());

		return new XCWorkspace(location.toPath(), projects, schemeNames);
	}

	public static XCWorkspaceReference of(Path location) {
		Preconditions.checkArgument(Files.exists(location), "Xcode workspace '%s' does not exists", location);
		Preconditions.checkArgument(Files.isDirectory(location), "Xcode workspace '%s' is not valid", location);
		return new XCWorkspaceReference(location);
	}

	@Override
	public String toString() {
		return location.toString();
	}
}
