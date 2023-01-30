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
import lombok.val;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.Map;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.fileRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.productRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.settings;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesMap;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXBuildFileTests extends CodeableAdapterTester<CodeablePBXBuildFile> {
	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXBuildFile.FileReference.class)
	void checkGetFileRef(PBXBuildFile.FileReference expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(fileRef)).then(doReturn(expectedValue))));

		assertThat(subject.getFileRef(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCSwiftPackageProductDependency.class)
	void checkGetProductRef(XCSwiftPackageProductDependency expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(productRef)).then(doReturn(expectedValue))));

		assertThat(subject.getProductRef(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.BuildSettingsProvider.class)
	void checkGetSettings(@ConvertWith(ToMap.class) Map<String, ?> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(settings)).then(doReturn(expectedValue))));

		assertThat(subject.getSettings(), matchesMap(expectedValue));
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
