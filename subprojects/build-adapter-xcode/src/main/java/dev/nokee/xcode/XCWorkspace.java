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

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
public final class XCWorkspace implements Serializable {
	private final File location;
	private final List<XCProjectReference> projects;
	private final ImmutableSet<String> schemeNames;

	// friends with XCWorkspaceReference
	XCWorkspace(Path workspaceLocation, List<XCProjectReference> projects, ImmutableSet<String> schemeNames) {
		assert Files.exists(workspaceLocation) && Files.isDirectory(workspaceLocation) : "invalid workspace";

		this.location = workspaceLocation.toFile();
		this.projects = projects;
		this.schemeNames = schemeNames;
	}

	public Path getLocation() {
		return location.toPath();
	}

	public List<XCProjectReference> getProjectLocations() {
		return projects;
	}

	public Set<String> getSchemeNames() {
		return schemeNames;
	}
}
