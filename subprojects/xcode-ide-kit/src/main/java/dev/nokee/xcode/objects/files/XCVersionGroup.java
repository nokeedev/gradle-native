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

import dev.nokee.utils.Optionals;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Represents a group that contains multiple file references to the different versions of a resource.
 * Users use this kind of group to contain the different versions of a {@literal xcdatamodel}.
 */
public final class XCVersionGroup extends PBXGroupElement implements GroupChild, PBXBuildFile.FileReference {
	@Nullable private final PBXFileReference currentVersion;
	@Nullable private final String versionGroupType;

	private XCVersionGroup(@Nullable String name, @Nullable String path, PBXSourceTree sourceTree, List<GroupChild> children, @Nullable PBXFileReference currentVersion, @Nullable String versionGroupType) {
		super(name, path, sourceTree, children);
		this.currentVersion = currentVersion;
		this.versionGroupType = versionGroupType;
	}

	public Optional<PBXFileReference> getCurrentVersion() {
		return Optional.ofNullable(currentVersion);
	}

	// Identifier of the group type
	// TODO: find better explaination
	public Optional<String> getVersionGroupType() {
		return Optionals.or(Optional.ofNullable(versionGroupType), () -> getCurrentVersion().flatMap(PBXFileReference::getExplicitFileType));
	}

	@Override
	public String toString() {
		return String.format(
			"%s isa=%s name=%s path=%s sourceTree=%s children=%s",
			super.toString(),
			this.getClass().getSimpleName(),
			getName().orElse(null),
			getPath().orElse(null),
			getSourceTree(),
			getChildren());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends PBXGroupElement.Builder<Builder, XCVersionGroup> {
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
			// TODO: Should we verify that currentVersion reference is part of the the children?
			return new XCVersionGroup(name, path, sourceTree, children, currentVersion, versionGroupType);
		}
	}
}
