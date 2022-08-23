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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MoreCollectors;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.project.PBXObjectArchiver;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.branch;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.exact;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.range;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.revision;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMinorVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class XCRemoteSwiftPackageReferenceVersionRequirementEncoderTest {
	@Test
	void canEncodeBranchVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(branch("release2.x")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "branch", "branch", "release2.x")));
	}

	@Test
	void canEncodeExactVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(exact("3.3")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "exactVersion", "version", "3.3")));
	}

	@Test
	void canEncodeRevisionVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(revision("4.3")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "revision", "revision", "4.3")));
	}

	@Test
	void canEncodeRangeVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(range("1.3", "1.4")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "versionRange", "minimumVersion", "1.3", "maximumVersion", "1.4")));
	}

	@Test
	void canEncodeUpToNextMinorVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(upToNextMinorVersion("3.6.1")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "upToNextMinorVersion", "minimumVersion", "3.6.1")));
	}

	@Test
	void canEncodeUpToNextMajorVersionRequirement() {
		val proj = new PBXObjectArchiver().encode(project(upToNextMajorVersion("7.6.2")));
		assertThat(proj.getObjects().get("XCRemoteSwiftPackageReference").collect(MoreCollectors.onlyElement()).getFields().get("requirement"),
			equalTo(ImmutableMap.of("kind", "upToNextMajorVersion", "minimumVersion", "7.6.2")));
	}

	private static PBXProject project(XCRemoteSwiftPackageReference.VersionRequirement requirement) {
		return PBXProject.builder().packageReference(XCRemoteSwiftPackageReference.builder().requirement(requirement).repositoryUrl("https://github.com/examplecom/example.git").build()).build();
	}
}
