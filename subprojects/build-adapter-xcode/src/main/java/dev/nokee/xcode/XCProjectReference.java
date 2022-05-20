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
import dev.nokee.xcode.project.PBXProjReader;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@EqualsAndHashCode
public final class XCProjectReference implements Serializable {
	private /*final*/ File location;

	private XCProjectReference(Path location) {
		this.location = location.toFile();
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
		return location.toString();
	}

	public XCProject load() {
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(getLocation().resolve("project.pbxproj"))))) {
			val pbxproj = reader.read();
			val targetIsa = ImmutableSet.of("PBXTarget", "PBXAggregateTarget", "PBXLegacyTarget", "PBXNativeTarget");
			val targetNames = Streams.stream(pbxproj.getObjects()).filter(it -> targetIsa.contains(it.isa())).map(it -> it.getFields().get("name").toString()).collect(ImmutableSet.toImmutableSet());
			return new XCProject(targetNames);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
