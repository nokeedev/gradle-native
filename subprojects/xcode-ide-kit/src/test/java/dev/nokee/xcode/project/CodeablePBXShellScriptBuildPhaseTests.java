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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeablePBXShellScriptBuildPhaseTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeablePBXShellScriptBuildPhase subject;

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildFilesProvider.class)
	void checkGetFiles(List<PBXBuildFile> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getFiles(), matchesIterable(expectedValue));
		verify(map).tryDecode(files);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getName(), matchesOptional(expectedValue));
		verify(map).tryDecode(name);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"/bin/bash"})
	void checkGetShellPath(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getShellPath(), matchesObject(expectedValue));
		verify(map).tryDecode(shellPath);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"some shell script\n"})
	void checkGetShellScript(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getShellScript(), matchesObject(expectedValue));
		verify(map).tryDecode(shellScript);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/Foo.c"})
	void checkGetInputPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getInputPaths(), matchesIterable(expectedValue));
		verify(map).tryDecode(inputPaths);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/MyInputs.xcfilelist"})
	void checkGetInputFileListPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getInputFileListPaths(), matchesIterable(expectedValue));
		verify(map).tryDecode(inputFileListPaths);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(BUILT_PRODUCT_DIR)/Output.lib"})
	void checkGetOutputPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getOutputPaths(), matchesIterable(expectedValue));
		verify(map).tryDecode(outputPaths);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"$(SOURCE_ROOT)/MyOutputs.xcfilelist"})
	void checkGetOutputFileListPaths(@AggregateWith(ToList.class) List<String> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getOutputFileListPaths(), matchesIterable(expectedValue));
		verify(map).tryDecode(outputFileListPaths);
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
