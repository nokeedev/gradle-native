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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Range;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class XCRemoteSwiftPackageReferenceVersionRequirementRangeTest {
	Range subject = range("2.4", "2.7");

	@Test
	void hasKind() {
		assertThat(subject.getKind(), equalTo(RANGE));
	}

	@Test
	void hasMinimumVersion() {
		assertThat(subject.getMinimumVersion(), equalTo("2.4"));
	}

	@Test
	void hasMaximumVersion() {
		assertThat(subject.getMaximumVersion(), equalTo("2.7"));
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("require range version from '2.4' to '2.7'"));
	}
}
