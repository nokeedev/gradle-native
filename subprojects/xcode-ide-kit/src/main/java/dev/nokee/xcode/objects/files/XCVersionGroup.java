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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeableXCVersionGroup;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Represents a group that contains multiple file references to the different versions of a resource.
 * Users use this kind of group to contain the different versions of a {@literal xcdatamodel}.
 */
public interface XCVersionGroup extends PBXGroupElement, GroupChild, PBXBuildFile.FileReference {
	Optional<PBXFileReference> getCurrentVersion();

	// Identifier of the group type
	// TODO: find better explaination
	Optional<String> getVersionGroupType();

	static Builder builder() {
		return new Builder();
	}

	final class Builder extends PBXGroupElement.Builder<Builder, XCVersionGroup> {
		private PBXFileReference currentVersion;
		private String versionGroupType;

		public Builder currentVersion(PBXFileReference currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}

		public Builder versionGroupType(String versionGroupType) {
			this.versionGroupType = versionGroupType;
			return this;
		}

		@Override
		protected XCVersionGroup newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<GroupChild> children) {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "XCVersionGroup");
			builder.put(CodeableXCVersionGroup.CodingKeys.name, name);
			builder.put(CodeableXCVersionGroup.CodingKeys.path, path);
			builder.put(CodeableXCVersionGroup.CodingKeys.sourceTree, sourceTree);
			builder.put(CodeableXCVersionGroup.CodingKeys.children, ImmutableList.copyOf(children));
			builder.put(CodeableXCVersionGroup.CodingKeys.currentVersion, currentVersion);

			String versionGroupType = this.versionGroupType;
			if (versionGroupType == null && currentVersion != null) {
				versionGroupType = currentVersion.getExplicitFileType().orElse(null);
			}
			builder.put(CodeableXCVersionGroup.CodingKeys.versionGroupType, versionGroupType);

			// TODO: Should we verify that currentVersion reference is part of the the children?
			return new CodeableXCVersionGroup(builder.build());
		}
	}
}
