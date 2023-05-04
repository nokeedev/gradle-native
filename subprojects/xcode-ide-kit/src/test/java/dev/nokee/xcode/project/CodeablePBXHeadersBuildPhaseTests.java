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
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase.CodingKeys.files;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXHeadersBuildPhaseTests extends CodeableAdapterTester<CodeablePBXHeadersBuildPhase> implements VisitableTester<PBXBuildPhase.Visitor<?>> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildFilesProvider.class)
	void checkGetFiles(List<PBXBuildFile> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(files)).then(doReturn(expectedValue))));

		assertThat(subject.getFiles(), matchesIterable(expectedValue));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}
}
