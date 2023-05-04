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
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.files;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXShellScriptBuildPhaseTests extends CodeableAdapterTester<CodeablePBXShellScriptBuildPhase> implements VisitableTester<PBXBuildPhase.Visitor<?>> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildFilesProvider.class)
	void checkGetFiles(List<PBXBuildFile> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(files)).then(doReturn(expectedValue))));

		assertThat(subject.getFiles(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(name)).then(doReturn(expectedValue))));

		assertThat(subject.getName(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"/bin/bash"})
	void checkGetShellPath(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(shellPath)).then(doReturn(expectedValue))));

		assertThat(subject.getShellPath(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"some shell script\n"})
	void checkGetShellScript(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(shellScript)).then(doReturn(expectedValue))));

		assertThat(subject.getShellScript(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/Foo.c"})
	void checkGetInputPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(inputPaths)).then(doReturn(expectedValue))));

		assertThat(subject.getInputPaths(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/MyInputs.xcfilelist"})
	void checkGetInputFileListPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(inputFileListPaths)).then(doReturn(expectedValue))));

		assertThat(subject.getInputFileListPaths(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(BUILT_PRODUCT_DIR)/Output.lib"})
	void checkGetOutputPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(outputPaths)).then(doReturn(expectedValue))));

		assertThat(subject.getOutputPaths(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/MyOutputs.xcfilelist"})
	void checkGetOutputFileListPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(outputFileListPaths)).then(doReturn(expectedValue))));

		assertThat(subject.getOutputFileListPaths(), matchesIterable(expectedValue));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}

	static final class ToList implements ArgumentsAggregator {
		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws ArgumentsAggregationException {
			if (accessor.get(0) == null) {
				return null;
			} else {
				return accessor.toList();
			}
		}
	}
}
