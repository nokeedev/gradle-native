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
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
public final class XCRemoteSwiftPackageReference extends PBXContainerItem {
	private final String repositoryUrl;
	private final VersionRequirement requirement;

	private XCRemoteSwiftPackageReference(String repositoryUrl, VersionRequirement requirement) {
		this.repositoryUrl = repositoryUrl;
		this.requirement = requirement;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public VersionRequirement getRequirement() {
		return requirement;
	}

	/**
	 * Represents a version requirement for a Swift package.
	 * There are three categories of requirement: version-based requirement, branch-based requirement, and commit-based requirement.
	 */
	public interface VersionRequirement {
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

		@EqualsAndHashCode
		final class Revision implements VersionRequirement {
			private String revision;

			public Revision(String revision) {
				this.revision = revision;
			}

			@Override
			public Kind getKind() {
				return Kind.REVISION;
			}

			public String getRevision() {
				return revision;
			}

			@Override
			public String toString() {
				return "require revision '" + revision + "'";
			}
		}

		static Revision revision(String revision) {
			return new Revision(Objects.requireNonNull(revision, "'revision' must not null"));
		}

		@EqualsAndHashCode
		final class Branch implements VersionRequirement {
			private String branch;

			public Branch(String branch) {
				this.branch = branch;
			}

			@Override
			public Kind getKind() {
				return Kind.BRANCH;
			}

			public String getBranch() {
				return branch;
			}

			@Override
			public String toString() {
				return "require branch '" + branch + "'";
			}
		}

		static Branch branch(String branchName) {
			return new Branch(Objects.requireNonNull(branchName, "'branchName' must not be null"));
		}

		@EqualsAndHashCode
		final class Exact implements VersionRequirement {
			private String version;

			public Exact(String version) {
				this.version = version;
			}

			@Override
			public Kind getKind() {
				return Kind.EXACT;
			}

			public String getVersion() {
				return version;
			}

			@Override
			public String toString() {
				return "require exact version '" + version + "'";
			}
		}

		static Exact exact(String version) {
			return new Exact(Objects.requireNonNull(version, "'version' must not be null"));
		}

		@EqualsAndHashCode
		final class Range implements VersionRequirement {
			private String minimumVersion;
			private String maximumVersion;

			public Range(String minimumVersion, String maximumVersion) {
				this.minimumVersion = minimumVersion;
				this.maximumVersion = maximumVersion;
			}

			@Override
			public Kind getKind() {
				return Kind.RANGE;
			}

			public String getMinimumVersion() {
				return minimumVersion;
			}

			public String getMaximumVersion() {
				return maximumVersion;
			}

			@Override
			public String toString() {
				return "require range version from '" + minimumVersion + "' to '" + maximumVersion + "'";
			}
		}

		static Range range(String fromVersion, String toVersion) {
			return new Range(Objects.requireNonNull(fromVersion, "'fromVersion' must not be null"), Objects.requireNonNull(toVersion, "'toVersion' must not be null"));
		}

		@EqualsAndHashCode
		final class UpToNextMinorVersion implements VersionRequirement {
			private final  String minimumVersion;

			public UpToNextMinorVersion(String minimumVersion) {
				this.minimumVersion = minimumVersion;
			}

			@Override
			public Kind getKind() {
				return Kind.UP_TO_NEXT_MINOR_VERSION;
			}

			public String getMinimumVersion() {
				return minimumVersion;
			}

			@Override
			public String toString() {
				return "require minimum version '" + minimumVersion + "' up to next minor version";
			}
		}

		static UpToNextMinorVersion upToNextMinorVersion(String minimumVersion) {
			return new UpToNextMinorVersion(Objects.requireNonNull(minimumVersion, "'minimumVersion' must not be null"));
		}

		@EqualsAndHashCode
		final class UpToNextMajorVersion implements VersionRequirement {
			private final String minimumVersion;

			private UpToNextMajorVersion(String minimumVersion) {
				this.minimumVersion = minimumVersion;
			}

			@Override
			public Kind getKind() {
				return Kind.UP_TO_NEXT_MAJOR_VERSION;
			}

			public String getMinimumVersion() {
				return minimumVersion;
			}

			@Override
			public String toString() {
				return "require minimum version '" + minimumVersion + "' up to next major version";
			}
		}

		static UpToNextMajorVersion upToNextMajorVersion(String minimumVersion) {
			return new UpToNextMajorVersion(Objects.requireNonNull(minimumVersion, "'minimumVersion' must not be null"));
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String repositoryUrl;
		private VersionRequirement requirement;

		public Builder repositoryUrl(String repositoryUrl) {
			this.repositoryUrl = repositoryUrl;
			return this;
		}

		public Builder requirement(VersionRequirement requirement) {
			this.requirement = requirement;
			return this;
		}

		public XCRemoteSwiftPackageReference build() {
			return new XCRemoteSwiftPackageReference(repositoryUrl, requirement);
		}
	}
}
