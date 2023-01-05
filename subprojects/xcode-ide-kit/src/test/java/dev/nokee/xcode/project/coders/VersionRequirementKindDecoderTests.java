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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.utils.ThrowingDecoderContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VersionRequirementKindDecoderTests {
	ValueDecoder.Context context = new ThrowingDecoderContext();
    VersionRequirementKindDecoder subject = new VersionRequirementKindDecoder();

	@ParameterizedTest
	@ArgumentsSource(KnownVersionRequirementKindProvider.class)
	void canDecodeKnownKind(String givenString, Kind knownKind) {
		assertThat(subject.decode(givenString, context), equalTo(knownKind));
	}

	@Test
	void throwsExceptionOnInvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode("unknownKind", context));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(CoderType.of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class)));
	}
}
