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

import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.children;
import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.currentVersion;
import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.name;
import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.path;
import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.sourceTree;
import static dev.nokee.xcode.project.CodeableXCVersionGroup.CodingKeys.versionGroupType;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeableXCVersionGroupTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeableXCVersionGroup subject;

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getName(), matchesOptional(expectedValue));
		verify(map).tryDecode(name);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXReferencePathsProvider.class)
	void checkGetPath(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getPath(), matchesOptional(expectedValue));
		verify(map).tryDecode(path);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {GroupChild.class, GroupChild.class, GroupChild.class})
	void checkGetChildren(List<GroupChild> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getChildren(), matchesIterable(expectedValue));
		verify(map).tryDecode(children);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXReferenceSourceTreesProvider.class)
	void checkGetSourceTree(PBXSourceTree expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getSourceTree(), matchesObject(expectedValue));
		verify(map).tryDecode(sourceTree);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXFileReference.class)
	void checkGetCurrentVersion(PBXFileReference expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getCurrentVersion(), matchesOptional(expectedValue));
		verify(map).tryDecode(currentVersion);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"group-type"})
	void checkGetCurrentVersion(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getVersionGroupType(), matchesOptional(expectedValue));
		verify(map).tryDecode(versionGroupType);
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
