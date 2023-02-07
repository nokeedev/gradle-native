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

import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.BRANCH;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.EXACT;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class VersionRequirementDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);
	VersionRequirementDecoder<?> subject = new VersionRequirementDecoder<>();

	@Nested
	class WhenBranchKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, BRANCH);

		@Test
		void createsBranch() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementBranch.newInstance(map)));
		}
	}

	@Nested
	class WhenExactKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, EXACT);

		@Test
		void createsExact() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementExact.newInstance(map)));
		}
	}

	@Nested
	class WhenRangeKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, RANGE);

		@Test
		void createsRange() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementRange.newInstance(map)));
		}
	}

	@Nested
	class WhenRevisionKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, REVISION);

		@Test
		void createsRevision() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementRevision.newInstance(map)));
		}
	}

	@Nested
	class WhenUpToNextMajorVersionKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, UP_TO_NEXT_MAJOR_VERSION);

		@Test
		void createsUpToNextMajorVersion() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementUpToNextMajorVersion.newInstance(map)));
		}
	}

	@Nested
	class WhenUpToNextMinorVersionKind {
		KeyedObject map = new SingleKeyedObject(KeyedCoders.VERSION_REQUIREMENT_KIND, UP_TO_NEXT_MINOR_VERSION);

		@Test
		void createsUpToNextMinorVersion() {
			assertThat(subject.decode(map, context), equalTo(CodeableVersionRequirementUpToNextMinorVersion.newInstance(map)));
		}
	}
}
