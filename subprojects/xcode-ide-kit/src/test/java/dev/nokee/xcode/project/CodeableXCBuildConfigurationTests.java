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

import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.files.PBXFileReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.xcode.project.CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference;
import static dev.nokee.xcode.project.CodeableXCBuildConfiguration.CodingKeys.buildSettings;
import static dev.nokee.xcode.project.CodeableXCBuildConfiguration.CodingKeys.name;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeableXCBuildConfigurationTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeableXCBuildConfiguration subject;

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getName(), matchesObject(expectedValue));
		verify(map).tryDecode(name);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXFileReference.class)
	void checkGetBaseConfigurationReference(PBXFileReference expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getBaseConfigurationReference(), matchesOptional(expectedValue));
		verify(map).tryDecode(baseConfigurationReference);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.BuildSettingsProvider.class)
	void checkGetBuildSettings(BuildSettings expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getBuildSettings(), matchesObject(expectedValue));
		verify(map).tryDecode(buildSettings);
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
