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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.branch;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.exact;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.range;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.revision;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMinorVersion;

class XCRemoteSwiftPackageReferenceVersionRequirementEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(range("1.0", "2.0"), range("1.0", "2.0"))
			.addEqualityGroup(range("1.1", "2.0"))
			.addEqualityGroup(range("1.0", "2.1"))
			.addEqualityGroup(revision("5.2"), revision("5.2"))
			.addEqualityGroup(revision("5.6"))
			.addEqualityGroup(exact("4.2"), exact("4.2"))
			.addEqualityGroup(exact("6.9"))
			.addEqualityGroup(branch("main"), branch("main"))
			.addEqualityGroup(branch("master"))
			.addEqualityGroup(upToNextMinorVersion("0.1.2"), upToNextMinorVersion("0.1.2"))
			.addEqualityGroup(upToNextMinorVersion("1.2.3"))
			.addEqualityGroup(upToNextMajorVersion("0.1.2"), upToNextMajorVersion("0.1.2"))
			.addEqualityGroup(upToNextMajorVersion("1.2.3"))
			.testEquals();
	}
}
