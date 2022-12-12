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
package dev.nokee.xcode.objects.swiftpackage;

import dev.nokee.xcode.objects.PBXContainerItem;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import dev.nokee.xcode.project.CodeableXCRemoteSwiftPackageReference;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import static java.util.Objects.requireNonNull;

public interface XCRemoteSwiftPackageReference extends PBXContainerItem {
	String getRepositoryUrl();

	VersionRequirement getRequirement();

	/**
	 * Represents a version requirement for a Swift package.
	 * There are three categories of requirement: version-based requirement, branch-based requirement, and commit-based requirement.
	 */
	interface VersionRequirement {
		Kind getKind();

		/**
		 * Represents {@link VersionRequirement} kinds.
		 */
		enum Kind {
			/**
			 * Use a specific revision (e.g. commit) of the Git repository.
			 *
			 * @see Revision
			 * @see VersionRequirement#revision(String)
			 */
			REVISION,

			/**
			 * Use a specific branch of the Git repository.
			 *
			 * @see Branch
			 * @see VersionRequirement#branch(String)
			 */
			BRANCH,

			/**
			 * Use the given package version.
			 *
			 * @see Exact
			 * @see VersionRequirement#exact(String)
			 */
			EXACT,

			/**
			 * Use a package version within the given range.
			 *
			 * @see Range
			 * @see VersionRequirement#range(String, String)
			 */
			RANGE,

			/**
			 * Use any package version up to the next minor version, e.g. major.minor.+.
			 *
			 * @see <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
			 * @see UpToNextMinorVersion
			 * @see VersionRequirement#upToNextMinorVersion(String)
			 */
			UP_TO_NEXT_MINOR_VERSION,

			/**
			 * Use any package version up to the next major version, e.g. major.+
			 *
			 * @see <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
			 * @see UpToNextMajorVersion
			 * @see VersionRequirement#upToNextMajorVersion(String)
			 */
			UP_TO_NEXT_MAJOR_VERSION
		}

		interface Revision extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.REVISION;
			}

			String getRevision();
		}

		static Revision revision(String revision) {
			return new CodeableVersionRequirementRevision(requireNonNull(revision, "'revision' must not null"));
		}

		interface Branch extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.BRANCH;
			}

			String getBranch();
		}

		static Branch branch(String branchName) {
			return new CodeableVersionRequirementBranch(requireNonNull(branchName, "'branchName' must not be null"));
		}

		interface Exact extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.EXACT;
			}

			String getVersion();
		}

		static Exact exact(String version) {
			return new CodeableVersionRequirementExact(requireNonNull(version, "'version' must not be null"));
		}

		interface Range extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.RANGE;
			}

			String getMinimumVersion();

			String getMaximumVersion();
		}

		static Range range(String fromVersion, String toVersion) {
			return new CodeableVersionRequirementRange(requireNonNull(fromVersion, "'fromVersion' must not be null"), requireNonNull(toVersion, "'toVersion' must not be null"));
		}

		interface UpToNextMinorVersion extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.UP_TO_NEXT_MINOR_VERSION;
			}

			String getMinimumVersion();
		}

		static UpToNextMinorVersion upToNextMinorVersion(String minimumVersion) {
			return new CodeableVersionRequirementUpToNextMinorVersion(requireNonNull(minimumVersion, "'minimumVersion' must not be null"));
		}

		interface UpToNextMajorVersion extends VersionRequirement {
			@Override
			default Kind getKind() {
				return Kind.UP_TO_NEXT_MAJOR_VERSION;
			}

			String getMinimumVersion();
		}

		static UpToNextMajorVersion upToNextMajorVersion(String minimumVersion) {
			return new CodeableVersionRequirementUpToNextMajorVersion(requireNonNull(minimumVersion, "'minimumVersion' must not be null"));
		}
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<XCRemoteSwiftPackageReference>, LenientAwareBuilder<Builder> {
		private String repositoryUrl;
		private VersionRequirement requirement;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "XCRemoteSwiftPackageReference");
			builder.requires(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder repositoryUrl(String repositoryUrl) {
			this.repositoryUrl = repositoryUrl;
			return this;
		}

		public Builder requirement(VersionRequirement requirement) {
			this.requirement = requirement;
			return this;
		}

		@Override
		public XCRemoteSwiftPackageReference build() {
			builder.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, repositoryUrl);
			builder.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, requirement);

			return new CodeableXCRemoteSwiftPackageReference(builder.build());
		}
	}
}
