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
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.buildConfigurationList;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.compatibilityVersion;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.mainGroup;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.packageReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.projectReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.targets;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXProjectTests extends CodeableAdapterTester<CodeablePBXProject> {
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"Xcode 3.0"})
	void checkGetCompatibilityVersion(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(compatibilityVersion)).then(doReturn(expectedValue))));

		assertThat(subject.getCompatibilityVersion(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCConfigurationList.class)
	void checkGetBuildConfigurationList(XCConfigurationList expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(buildConfigurationList)).then(doReturn(expectedValue))));

		assertThat(subject.getBuildConfigurationList(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXGroup.class)
	void checkGetMainGroup(PBXGroup expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(mainGroup)).then(doReturn(expectedValue))));

		assertThat(subject.getMainGroup(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {PBXProject.ProjectReference.class, PBXProject.ProjectReference.class, PBXProject.ProjectReference.class})
	void checkGetProjectReferences(List<PBXProject.ProjectReference> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(projectReferences)).then(doReturn(expectedValue))));

		assertThat(subject.getProjectReferences(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {XCRemoteSwiftPackageReference.class, XCRemoteSwiftPackageReference.class, XCRemoteSwiftPackageReference.class})
	void checkGetPackageReferences(List<XCRemoteSwiftPackageReference> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(packageReferences)).then(doReturn(expectedValue))));

		assertThat(subject.getPackageReferences(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {PBXTarget.class, PBXTarget.class, PBXTarget.class})
	void checkGetTargets(List<PBXTarget> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(targets)).then(doReturn(expectedValue))));

		assertThat(subject.getTargets(), matchesIterable(expectedValue));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}
}
