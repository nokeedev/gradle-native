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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXVariantGroup.CodingKeys.children;
import static dev.nokee.xcode.project.CodeablePBXVariantGroup.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXVariantGroup.CodingKeys.path;
import static dev.nokee.xcode.project.CodeablePBXVariantGroup.CodingKeys.sourceTree;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class CodeablePBXVariantGroupTests extends CodeableAdapterTester<CodeablePBXVariantGroup> {
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
	@NullSource
	@MockitoSource(listOf = {GroupChild.class, GroupChild.class, GroupChild.class})
	void checkGetChildren(List<GroupChild> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(children)).then(doReturn(expectedValue))));

		assertThat(subject.getChildren(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXReferenceSourceTreesProvider.class)
	void checkGetSourceTree(PBXSourceTree expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(sourceTree)).then(doReturn(expectedValue))));

		assertThat(subject.getSourceTree(), matchesObject(expectedValue));
	}

	@Test
	void isPBXReference() {
		assertThat(newSubject(), isA(PBXReference.class));
	}

	@Test
	void isFileReference() {
		assertThat(newSubject(), isA(PBXBuildFile.FileReference.class));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}

	@Nested
	class FileReferenceVisitorTests implements VisitableTester<PBXBuildFile.FileReference.Visitor<?>> {
		@Override
		public Object newSubject() {
			return CodeablePBXVariantGroupTests.this.newSubject();
		}
	}

	@Nested
	class ChildGroupVisitorTests implements VisitableTester<GroupChild.Visitor<?>> {
		@Override
		public Object newSubject() {
			return CodeablePBXVariantGroupTests.this.newSubject();
		}
	}
}
