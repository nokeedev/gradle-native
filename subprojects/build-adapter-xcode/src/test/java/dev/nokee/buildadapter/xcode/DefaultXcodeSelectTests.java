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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodeSelect;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.CommandLineToolExecutionHandle;
import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.core.exec.CommandLineToolLogContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.nokee.fixtures.exec.CommandLineToolMatchers.commandLine;
import static dev.nokee.fixtures.exec.CommandLineToolMatchers.discardedOutputs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class DefaultXcodeSelectTests {
	@Mock CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable> engine;
	@Mock CommandLineToolExecutionResult result;
	@Captor ArgumentCaptor<CommandLineToolInvocation> invocationCapture;
	CommandLineToolExecutionHandle.Waitable handle = () -> result;
	DefaultXcodeSelect subject;

	@BeforeEach
	void givenSubject() {
		subject = new DefaultXcodeSelect(engine);
		Mockito.when(engine.submit(invocationCapture.capture())).thenReturn(handle);
	}

	@Nested
	class DeveloperDirectory {
		/*static*/ final Path EXPECTED_DEVELOPER_DIRECTORY = Paths.get("/Applications/Xcode.app/Contents/Developer");

		@BeforeEach
		void setup() {
			Mockito.when(result.getOutput()).thenReturn(CommandLineToolLogContent.of(EXPECTED_DEVELOPER_DIRECTORY + "\n"));
		}

		@Test
		void executesXcodeSelectWithPrintPathFlagAndDiscardedOutput() {
			assertThat("returns printed developer directory", subject.developerDirectory(),
				equalTo(EXPECTED_DEVELOPER_DIRECTORY));
			assertThat("executes xcode-select command", invocationCapture.getValue(),
				commandLine(contains("xcode-select", "--print-path")));
			assertThat("discards outputs", invocationCapture.getValue(), discardedOutputs());
		}
	}
}
