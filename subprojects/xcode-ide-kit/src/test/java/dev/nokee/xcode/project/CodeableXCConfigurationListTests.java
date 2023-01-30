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

import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeableXCConfigurationList.CodingKeys.buildConfigurations;
import static dev.nokee.xcode.project.CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible;
import static dev.nokee.xcode.project.CodeableXCConfigurationList.CodingKeys.defaultConfigurationName;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesBoolean;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeableXCConfigurationListTests extends CodeableAdapterTester<CodeableXCConfigurationList> {
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"Release"})
	void checkGetDefaultConfigurationName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(defaultConfigurationName)).then(doReturn(expectedValue))));

		assertThat(subject.getDefaultConfigurationName(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {XCBuildConfiguration.class, XCBuildConfiguration.class, XCBuildConfiguration.class})
	void checkGetBuildConfigurations(List<XCBuildConfiguration> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(buildConfigurations)).then(doReturn(expectedValue))));

		assertThat(subject.getBuildConfigurations(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(booleans = {true, false})
	void checkIsDefaultConfigurationIsVisible(Boolean expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(defaultConfigurationIsVisible)).then(doReturn(expectedValue))));

		assertThat(subject.isDefaultConfigurationIsVisible(), matchesBoolean(expectedValue, false));
	}
}
