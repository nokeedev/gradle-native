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

import static org.apache.commons.io.FilenameUtils.removeExtension;

@EqualsAndHashCode
public final class DefaultXCWorkspaceReference implements XCWorkspaceReference, Serializable {
	private final File location;

	public DefaultXCWorkspaceReference(Path location) {
		this.location = location.toFile();
	}

	@Override
	public String getName() {
		return FilenameUtils.removeExtension(location.getName());
	}

	@Override
	public Path getLocation() {
		return location.toPath();
	}

	@Override
	public XCWorkspace load() {
		return XCCache.cacheIfAbsent(this, key -> {
			List<XCProjectReference> projects = ImmutableList.copyOf(XCLoaders.workspaceProjectReferencesLoader().load(this));

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

			return new DefaultXCWorkspace(location.toPath(), projects, schemeNames);
		});
	}

	@Override
	public String toString() {
		return "workspace '" + location.toString() + "'";
	}
}
