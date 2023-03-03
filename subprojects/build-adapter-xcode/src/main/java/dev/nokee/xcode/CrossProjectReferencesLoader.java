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
import lombok.EqualsAndHashCode;
import lombok.val;

import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode
public final class CrossProjectReferencesLoader implements XCLoader<Iterable<XCProjectReference>, XCProjectReference>, Serializable {
	private final XCLoader<PBXProject, XCProjectReference> pbxLoader;
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;

	public CrossProjectReferencesLoader(XCLoader<PBXProject, XCProjectReference> pbxLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader) {
		this.pbxLoader = pbxLoader;
		this.fileReferencesLoader = fileReferencesLoader;
	}

	@Override
	public Iterable<XCProjectReference> load(XCProjectReference reference) {
		val project = pbxLoader.load(reference);

		return project.getProjectReferences().stream()
			.map(PBXProject.ProjectReference::getProjectReference)
			.map(it -> fileReferencesLoader.load(reference).get(it))
			.map(it -> it.resolve(new XCFileReference.ResolveContext() {
				@Override
				public Path getBuiltProductsDirectory() {
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
	}
}
