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
import dev.nokee.xcode.objects.PBXProject;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.util.Set;

import static dev.nokee.xcode.XCTargetReference.walk;

@EqualsAndHashCode
public final class XCProject {
	private final Path location;
	private final ImmutableSet<XCTargetReference> targets;
	private final ImmutableSet<String> schemeNames;
	private transient final PBXProject project;
	private transient XCTargetReference.XCFileReferences references;

	// friends with XCProjectReference
	XCProject(Path location, ImmutableSet<XCTargetReference> targets, ImmutableSet<String> schemeNames, PBXProject project) {
		this.location = location;
		this.targets = targets;
		this.schemeNames = schemeNames;
		this.project = project;
	}

	public Set<XCTargetReference> getTargets() {
		return targets;
	}

	public Set<String> getSchemeNames() {
		return schemeNames;
	}

	public XCProjectReference toReference() {
		return XCProjectReference.of(location);
	}

	PBXProject getModel() {
		return project;
	}

	XCTargetReference.XCFileReferences getFileReferences() {
		if (references == null) {
			references = walk(project);
		}
		return references;
	}
}
