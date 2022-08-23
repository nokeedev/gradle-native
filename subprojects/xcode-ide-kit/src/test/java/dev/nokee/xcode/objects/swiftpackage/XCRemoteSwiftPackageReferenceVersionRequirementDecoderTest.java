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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProj;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.branch;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.exact;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.range;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.revision;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMinorVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XCRemoteSwiftPackageReferenceVersionRequirementDecoderTest {
	@Test
	void canDecodeBranchVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "branch", "branch", "release")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(branch("release")));
	}

	@Test
	void throwsExceptionWhenDecodingBranchVersionRequirementWithMissingBranchField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "branch"))));
	}

	@Test
	void canDecodeExactVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "exactVersion", "version", "3.4.5")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(exact("3.4.5")));
	}

	@Test
	void throwsExceptionWhenDecodingExactVersionRequirementWithMissingVersionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "exactVersion"))));
	}

	@Test
	void canDecodeRevisionVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "revision", "revision", "5.6")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(revision("5.6")));
	}

	@Test
	void throwsExceptionWhenDecodingRevisionVersionRequirementWithMissingRevisionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "revision"))));
	}

	@Test
	void canDecodeUpToNextMinorVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "upToNextMinorVersion", "minimumVersion", "2.3")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(upToNextMinorVersion("2.3")));
	}

	@Test
	void throwsExceptionWhenDecodingUpToNextMinorVersionRequirementWithMissingMinimumVersionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "upToNextMinorVersion"))));
	}

	@Test
	void canDecodeUpToNextMajorVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "upToNextMajorVersion", "minimumVersion", "4.2")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(upToNextMajorVersion("4.2")));
	}

	@Test
	void throwsExceptionWhenDecodingUpToNextMajorVersionRequirementWithMissingMinimumVersionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "upToNextMajorVersion"))));
	}

	@Test
	void canDecodeRangeVersionRequirement() {
		val project = new PBXObjectUnarchiver().decode(project(of("kind", "versionRange", "minimumVersion", "1.0", "maximumVersion", "2.0")));
		assertThat(project.getPackageReferences().get(0).getRequirement(), equalTo(range("1.0", "2.0")));
	}

	@Test
	void throwsExceptionWhenDecodingRangeVersionRequirementWithMissingMinimumVersionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "versionRange", "maximumVersion", "5.0"))));
	}

	@Test
	void throwsExceptionWhenDecodingRangeVersionRequirementWithMissingMaximumVersionField() {
		assertThrows(RuntimeException.class, () -> new PBXObjectUnarchiver().decode(project(of("kind", "versionRange", "minimum", "2.0"))));
	}

	private static PBXProj project(Map<String, ?> requirement) {
		return PBXProj.builder().objects(builder -> {
			builder.add(PBXObjectReference.of("1", it -> {
				it.putField("isa", "XCRemoteSwiftPackageReference");
				it.putField("requirement", requirement);
				it.putField("repositoryURL", "https://github.com/examplecom/example.git");
			}));
			builder.add(PBXObjectReference.of("2", it -> {
				it.putField("isa", "PBXProject");
				it.putField("packageReferences", ImmutableList.of("1"));
			}));
		}).rootObject("2").build();
	}
}
