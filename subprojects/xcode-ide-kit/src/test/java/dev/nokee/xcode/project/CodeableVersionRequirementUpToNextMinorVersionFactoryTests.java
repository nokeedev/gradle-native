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
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMinorVersion;
import static dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind;
import static dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CodeableVersionRequirementUpToNextMinorVersionFactoryTests {
	@Test
	void canCreateUpToNextMinorVersionRequirement() {
		assertThat(upToNextMinorVersion("7.6"),
			equalTo(new CodeableVersionRequirementUpToNextMinorVersion(new DefaultKeyedObject(of(kind, UP_TO_NEXT_MINOR_VERSION, minimumVersion, "7.6")))));
	}
}
