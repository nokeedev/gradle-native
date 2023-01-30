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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXTargetDependency.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXTargetDependency.CodingKeys.target;
import static dev.nokee.xcode.project.CodeablePBXTargetDependency.CodingKeys.targetProxy;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXTargetDependencyTests extends CodeableAdapterTester<CodeablePBXTargetDependency> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(name)).then(doReturn(expectedValue))));

		assertThat(subject.getName(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXTarget.class)
	void checkGetTarget(PBXTarget expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(target)).then(doReturn(expectedValue))));

		assertThat(subject.getTarget(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXContainerItemProxy.class)
	void checkGetTargetProxy(PBXContainerItemProxy expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(targetProxy)).then(doReturn(expectedValue))));

		assertThat(subject.getTargetProxy(), matchesObject(expectedValue));
	}
}
