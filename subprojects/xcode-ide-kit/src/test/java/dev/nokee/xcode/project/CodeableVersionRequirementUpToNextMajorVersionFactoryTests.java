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
package dev.nokee.xcode.project;

import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind;
import static dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CodeableVersionRequirementUpToNextMajorVersionFactoryTests {
	@Test
	void canCreateUpToNextMajorVersionRequirement() {
		assertThat(upToNextMajorVersion("8.0"),
			equalTo(new CodeableVersionRequirementUpToNextMajorVersion(new DefaultKeyedObject(of(kind, UP_TO_NEXT_MAJOR_VERSION, minimumVersion, "8.0")))));
	}
}
