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

import com.google.common.collect.Streams;
import dev.nokee.xcode.objects.PBXContainerItem;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

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

	public interface VersionRequirement {
		Kind getKind();

		enum Kind {
			REVISION("revison"), BRANCH("branch"), EXACT("exact"), RANGE("range"), UP_TO_NEXT_MINOR_VERSION("upToNextMinorVersion"), UP_TO_NEXT_MAJOR_VERSION("upToNextMajorVersion");

			private String value;

			Kind(String value) {
				this.value = value;
			}

			public static Kind of(String value) {
				return Arrays.stream(values()).filter(it -> it.value.equals(value)).findFirst().orElseThrow(RuntimeException::new);
			}

			public String toString() {
				return value;
			}
		}

		final class Revision implements VersionRequirement {
			@Override
			public Kind getKind() {
				return Kind.REVISION;
			}
		}

		final class Branch implements VersionRequirement {
			@Override
			public Kind getKind() {
				return Kind.BRANCH;
			}
		}

		final class Exact implements VersionRequirement {
			@Override
			public Kind getKind() {
				return Kind.EXACT;
			}
		}

		final class Range implements VersionRequirement {
			@Override
			public Kind getKind() {
				return Kind.RANGE;
			}
		}

		final class UpToNextMinorVersion implements VersionRequirement {
			@Override
			public Kind getKind() {
				return Kind.UP_TO_NEXT_MINOR_VERSION;
			}
		}

		@EqualsAndHashCode
		final class UpToNextMajorVersion implements VersionRequirement {
			private String minimumVersion;

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
		}

		static UpToNextMajorVersion upToNextMajorVersion(String minimumVersion) {
			return new UpToNextMajorVersion(minimumVersion);
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
