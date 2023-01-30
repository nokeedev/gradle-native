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

import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXFileReference.CodingKeys.explicitFileType;
import static dev.nokee.xcode.project.CodeablePBXFileReference.CodingKeys.lastKnownFileType;
import static dev.nokee.xcode.project.CodeablePBXFileReference.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXFileReference.CodingKeys.path;
import static dev.nokee.xcode.project.CodeablePBXFileReference.CodingKeys.sourceTree;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class CodeablePBXFileReferenceTests extends CodeableAdapterTester<CodeablePBXFileReference> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(name)).then(doReturn(expectedValue))));

		assertThat(subject.getName(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXReferencePathsProvider.class)
	void checkGetPath(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(path)).then(doReturn(expectedValue))));

		assertThat(subject.getPath(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXReferenceSourceTreesProvider.class)
	void checkGetSourceTree(PBXSourceTree expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(sourceTree)).then(doReturn(expectedValue))));

		assertThat(subject.getSourceTree(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"com.example.my-file"})
	void checkGetLastKnownFileType(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(lastKnownFileType)).then(doReturn(expectedValue))));

		assertThat(subject.getLastKnownFileType(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"com.example.my-other-file"})
	void checkGetExplicitFileType(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(explicitFileType)).then(doReturn(expectedValue))));

		assertThat(subject.getExplicitFileType(), matchesOptional(expectedValue));
	}

	@Test
	void isPBXReference() {
		assertThat(newSubject(), isA(PBXReference.class));
	}
}
