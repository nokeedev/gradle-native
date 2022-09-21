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
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.xcode.DefaultXCTargetReference.walk;

public final class CrossProjectReferencesLoader implements XCLoader<Iterable<XCProjectReference>, XCProjectReference> {
	@Override
	public Iterable<XCProjectReference> load(XCProjectReference reference) {
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(reference.getLocation().resolve("project.pbxproj"))))) {
			val pbxproj = reader.read();
			val project = new PBXObjectUnarchiver().decode(pbxproj);

			val fileReferences = walk(project);

			return project.getProjectReferences().stream()
				.map(PBXProject.ProjectReference::getProjectReference)
				.map(it -> fileReferences.get(it))
				.map(it -> it.resolve(new XCFileReference.ResolveContext() {
					@Override
					public Path getBuiltProductDirectory() {
						throw new UnsupportedOperationException("Should not call");
					}

					@Override
					public Path get(String name) {
						if ("SOURCE_ROOT".equals(name)) {
							return reference.getLocation().getParent();
						}
						throw new UnsupportedOperationException(String.format("Could not resolve '%s' build setting.", name));
					}
				}))
				.map(XCProjectReference::of)
				.collect(ImmutableList.toImmutableList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
