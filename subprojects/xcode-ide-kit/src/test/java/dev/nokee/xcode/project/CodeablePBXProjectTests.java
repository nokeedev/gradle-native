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

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.PBXTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.buildConfigurationList;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.compatibilityVersion;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.mainGroup;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.packageReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.projectReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.targets;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeablePBXProjectTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeablePBXProject subject;

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"Xcode 3.0"})
	void checkGetCompatibilityVersion(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getCompatibilityVersion(), matchesObject(expectedValue));
		verify(map).tryDecode(compatibilityVersion);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCConfigurationList.class)
	void checkGetBuildConfigurationList(XCConfigurationList expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getBuildConfigurationList(), matchesObject(expectedValue));
		verify(map).tryDecode(buildConfigurationList);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXGroup.class)
	void checkGetMainGroup(PBXGroup expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getMainGroup(), matchesObject(expectedValue));
		verify(map).tryDecode(mainGroup);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {PBXProject.ProjectReference.class, PBXProject.ProjectReference.class, PBXProject.ProjectReference.class})
	void checkGetProjectReferences(List<PBXProject.ProjectReference> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getProjectReferences(), matchesIterable(expectedValue));
		verify(map).tryDecode(projectReferences);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {XCRemoteSwiftPackageReference.class, XCRemoteSwiftPackageReference.class, XCRemoteSwiftPackageReference.class})
	void checkGetPackageReferences(List<XCRemoteSwiftPackageReference> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getPackageReferences(), matchesIterable(expectedValue));
		verify(map).tryDecode(packageReferences);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {PBXTarget.class, PBXTarget.class, PBXTarget.class})
	void checkGetTargets(List<PBXTarget> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getTargets(), matchesIterable(expectedValue));
		verify(map).tryDecode(targets);
	}

	@Test
	void forwardsEncodingToDelegate() {
		Codeable.EncodeContext context = mock(Codeable.EncodeContext.class);
		subject.encode(context);
		verify(map).encode(context);
	}

	@Test
	void forwardsIsaToDelegate() {
		subject.isa();
		verify(map).isa();
	}

	@Test
	void forwardsGlobalIdToDelegate() {
		subject.globalId();
		verify(map).globalId();
	}

	@Test
	void forwardsTryDecodeToDelegate() {
		CodingKey key = mock(CodingKey.class);
		subject.tryDecode(key);
		verify(map).tryDecode(key);
	}
}
