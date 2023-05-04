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

import dev.nokee.internal.testing.testdoubles.MockitoBuilder;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeableVersionRequirementBranch.CodingKeys.branch;
import static dev.nokee.xcode.project.CodeableVersionRequirementBranch.CodingKeys.kind;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class CodeableVersionRequirementBranchTests extends CodeableAdapterTester<CodeableVersionRequirementBranch> implements VisitableTester<XCRemoteSwiftPackageReference.VersionRequirement.Visitor<?>> {
	@ParameterizedTest
	@NullSource
	@EnumSource(value = Kind.class, names = "BRANCH")
	void checkGetKind(Kind expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(kind)).then(doReturn(expectedValue))));

		assertThat(subject.getKind(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = "master")
	void checkGetBranch(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(branch)).then(doReturn(expectedValue))));

		assertThat(subject.getBranch(), matchesObject(expectedValue));
	}

	@Test
	void checkToString() {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(branch)).then(doReturn("develop"))));

		assertThat(subject, hasToString("require branch 'develop'"));
	}

	@Test
	void encodesKnownCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeKnownCodingKeys(delegate));
	}
}
