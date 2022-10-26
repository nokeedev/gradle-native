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
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.revision;
import static dev.nokee.xcode.project.CodeableVersionRequirementRevision.CodingKeys.kind;
import static dev.nokee.xcode.project.CodeableVersionRequirementRevision.CodingKeys.revision;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CodeableVersionRequirementRevisionFactoryTests {
	@Test
	void canCreateRevisionRequirement() {
		assertThat(revision("b5ef5b968ab4e87856bfab5cdf966dee59dbc30b"),
			equalTo(new CodeableVersionRequirementRevision(new DefaultKeyedObject(of(kind, REVISION, revision, "b5ef5b968ab4e87856bfab5cdf966dee59dbc30b")))));
	}
}
