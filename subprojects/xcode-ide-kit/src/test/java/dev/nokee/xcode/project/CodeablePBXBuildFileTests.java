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
import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.fileRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.productRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.settings;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesMap;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeablePBXBuildFileTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeablePBXBuildFile subject;

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXBuildFile.FileReference.class)
	void checkGetFileRef(PBXBuildFile.FileReference expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getFileRef(), matchesOptional(expectedValue));
		verify(map).tryDecode(fileRef);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCSwiftPackageProductDependency.class)
	void checkGetProductRef(XCSwiftPackageProductDependency expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getProductRef(), matchesOptional(expectedValue));
		verify(map).tryDecode(productRef);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.BuildSettingsProvider.class)
	void checkGetSettings(@ConvertWith(ToMap.class) Map<String, ?> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getSettings(), matchesMap(expectedValue));
		verify(map).tryDecode(settings);
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

	static class ToMap implements ArgumentConverter {
		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			if (source == null) {
				return null;
			} else {
				return ((BuildSettings) source).asMap();
			}
		}
	}
}
