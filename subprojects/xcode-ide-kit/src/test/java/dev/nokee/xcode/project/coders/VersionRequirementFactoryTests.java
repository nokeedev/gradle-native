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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.BRANCH;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.EXACT;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionRequirementFactoryTests {
	@Mock KeyedObject map;
	@InjectMocks VersionRequirementFactory<?> subject;

	@Nested
	class WhenBranchKind {
		@BeforeEach
		void givenBranchKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(BRANCH);
		}

		@Test
		void createsBranch() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementBranch.newInstance(map)));
		}
	}

	@Nested
	class WhenExactKind {
		@BeforeEach
		void givenExactKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(EXACT);
		}

		@Test
		void createsExact() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementExact.newInstance(map)));
		}
	}

	@Nested
	class WhenRangeKind {
		@BeforeEach
		void givenRangeKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(RANGE);
		}

		@Test
		void createsRange() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementRange.newInstance(map)));
		}
	}

	@Nested
	class WhenRevisionKind {
		@BeforeEach
		void givenRevisionKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(REVISION);
		}

		@Test
		void createsRevision() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementRevision.newInstance(map)));
		}
	}

	@Nested
	class WhenUpToNextMajorVersionKind {
		@BeforeEach
		void givenUpToNextMajorVersionKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(UP_TO_NEXT_MAJOR_VERSION);
		}

		@Test
		void createsUpToNextMajorVersion() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementUpToNextMajorVersion.newInstance(map)));
		}
	}

	@Nested
	class WhenUpToNextMinorVersionKind {
		@BeforeEach
		void givenUpToNextMinorVersionKind() {
			when(map.tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)).thenReturn(UP_TO_NEXT_MINOR_VERSION);
		}

		@Test
		void createsUpToNextMinorVersion() {
			assertThat(subject.create(map), equalTo(CodeableVersionRequirementUpToNextMinorVersion.newInstance(map)));
		}
	}
}
