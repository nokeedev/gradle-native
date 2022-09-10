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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
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

import static org.apache.commons.io.FilenameUtils.removeExtension;

@EqualsAndHashCode
public final class XCProjectReference implements Serializable {
	private /*final*/ File location;

	private XCProjectReference(Path location) {
		this.location = location.toFile();
	}

	public String getName() {
		return FilenameUtils.removeExtension(location.getName());
	}

	public Path getLocation() {
		return location.toPath();
	}

	public static XCProjectReference of(Path location) {
		Preconditions.checkArgument(Files.exists(location), "Xcode project '%s' does not exists", location);
		Preconditions.checkArgument(Files.isDirectory(location), "Xcode project '%s' is not valid", location);
		return new XCProjectReference(location);
	}

	@Override
	public String toString() {
		return "project '" + location + "'";
	}

	public XCProject load() {
		return XCCache.cacheIfAbsent(this, key -> {
			try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(getLocation().resolve("project.pbxproj"))))) {
				val pbxproj = reader.read();
				val proj = new PBXObjectUnarchiver().decode(pbxproj);

				val targetIsa = ImmutableSet.of("PBXAggregateTarget", "PBXLegacyTarget", "PBXNativeTarget");
				val targets = Streams.stream(pbxproj.getObjects()).filter(it -> targetIsa.contains(it.isa())).map(it -> it.getFields().get("name").toString()).map(name -> XCTargetReference.of(this, name)).collect(ImmutableSet.toImmutableSet());

				val it = getLocation().resolve("xcshareddata/xcschemes");
				val builder = ImmutableSet.<String>builder();
				if (Files.isDirectory(it)) {
					try (final DirectoryStream<Path> xcodeSchemeStream = Files.newDirectoryStream(it, "*.xcscheme")) {
						for (Path xcodeSchemeFile : xcodeSchemeStream) {
							builder.add(removeExtension(xcodeSchemeFile.getFileName().toString()));
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				val schemeNames = builder.build();

				// TODO: Add support for implicit scheme: xcodebuild -list -project `getLocation()` -json
				return new XCProject(getName(), getLocation(), targets, schemeNames, proj);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
