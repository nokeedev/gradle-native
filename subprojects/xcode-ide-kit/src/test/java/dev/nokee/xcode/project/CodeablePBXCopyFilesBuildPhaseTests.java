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

import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath;
import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec;
import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.files;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeablePBXCopyFilesBuildPhaseTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeablePBXCopyFilesBuildPhase subject;

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildFilesProvider.class)
	void checkGetFiles(List<PBXBuildFile> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getFiles(), matchesIterable(expectedValue));
		verify(map).tryDecode(files);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"some/path"})
	void checkGetDstPath(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getDstPath(), matchesObject(expectedValue));
		verify(map).tryDecode(dstPath);
	}

	@ParameterizedTest
	@NullSource
	@EnumSource(PBXCopyFilesBuildPhase.SubFolder.class)
	void checkGetDstSubfolderSpec(PBXCopyFilesBuildPhase.SubFolder expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getDstSubfolderSpec(), matchesObject(expectedValue));
		verify(map).tryDecode(dstSubfolderSpec);
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
