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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.project.ValueEncoder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class VersionRequirementKindEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
    VersionRequirementKindEncoder subject = new VersionRequirementKindEncoder();

	@ParameterizedTest
	@ArgumentsSource(KnownVersionRequirementKindProvider.class)
	void canEncodeKnownKind(String expectedString, Kind knownKind) {
		assertThat(subject.encode(knownKind, context), equalTo(expectedString));
	}
}
