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
package dev.nokee.xcode.objects.files;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Represents a group that contains multiple file references to the different versions of a resource.
 * Users use this kind of group to contain the different versions of a {@literal xcdatamodel}.
 */
public interface XCVersionGroup extends GroupChild, PBXBuildFile.FileReference {
	List<GroupChild> getChildren();

	Optional<PBXFileReference> getCurrentVersion();

	// Identifier of the group type
	// TODO: find better explaination
	Optional<String> getVersionGroupType();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<XCVersionGroup>, LenientAwareBuilder<Builder> {
		private PBXFileReference currentVersion;
		private String versionGroupType;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "XCVersionGroup");
			builder.requires(key(CodeableXCVersionGroup.CodingKeys.sourceTree));
			// mainGroup can have both null name and path

			// Default values
			builder.ifAbsent(CodeableXCVersionGroup.CodingKeys.sourceTree, PBXSourceTree.GROUP);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeableXCVersionGroup.CodingKeys.name, name);
			return this;
		}

		public Builder path(String path) {
			builder.put(CodeableXCVersionGroup.CodingKeys.path, path);
			return this;
		}

		public Builder child(GroupChild reference) {
			assertValid(reference);
			builder.add(CodeableXCVersionGroup.CodingKeys.children, reference);
			return this;
		}

		private void assertValid(GroupChild reference) {
			if (!reference.getName().isPresent() && !reference.getPath().isPresent()) {
				throw new NullPointerException("either 'name' or 'path' must not be null for non-main PBXGroup");
			}
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			builder.put(CodeableXCVersionGroup.CodingKeys.sourceTree, sourceTree);
			return this;
		}

		public Builder children(Iterable<? extends GroupChild> references) {
			builder.put(CodeableXCVersionGroup.CodingKeys.children, stream(references).peek(this::assertValid).collect(ImmutableList.toImmutableList()));
			return this;
		}

		public Builder currentVersion(PBXFileReference currentVersion) {
			this.currentVersion = currentVersion;
			builder.put(CodeableXCVersionGroup.CodingKeys.currentVersion, currentVersion);
			return this;
		}

		public Builder versionGroupType(String versionGroupType) {
			this.versionGroupType = versionGroupType;
			return this;
		}

		@Override
		public XCVersionGroup build() {
			String versionGroupType = this.versionGroupType;
			if (versionGroupType == null && currentVersion != null) {
				versionGroupType = currentVersion.getExplicitFileType().orElse(null);
			}
			builder.putNullable(CodeableXCVersionGroup.CodingKeys.versionGroupType, versionGroupType);

			// TODO: Should we verify that currentVersion reference is part of the the children?
			return new CodeableXCVersionGroup(builder.build());
		}
	}
}
