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

import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodebuild;
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

import static dev.nokee.fixtures.exec.CommandLineToolMatchers.commandLine;
import static dev.nokee.fixtures.exec.CommandLineToolMatchers.discardedOutputs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class DefaultXcodebuildTests {
	@Mock CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable> engine;
	@Mock CommandLineToolExecutionResult result;
	@Captor ArgumentCaptor<CommandLineToolInvocation> invocationCapture;
	CommandLineToolExecutionHandle.Waitable handle = () -> result;
	DefaultXcodebuild subject;

	@BeforeEach
	void givenSubject() {
		subject = new DefaultXcodebuild(engine);
		Mockito.when(engine.submit(invocationCapture.capture())).thenReturn(handle);
	}

	@Nested
	class Version {
		/*static*/ final String EXPECTED_XCODE_VERSION = "14.0";
		/*static*/ final String EXPECTED_XCODE_BUILD_VERSION = "14A309";

		@BeforeEach
		void setup() {
			Mockito.when(result.getOutput()).thenReturn(CommandLineToolLogContent.of("Xcode " + EXPECTED_XCODE_VERSION + "\nBuild version " + EXPECTED_XCODE_BUILD_VERSION + "\n"));
		}

		@Test
		void executesXcodebuildWithVersionFlagAndDiscardedOutput() {
			assertThat("returns Xcode version", subject.version(), equalTo(EXPECTED_XCODE_VERSION));
			assertThat("executes xcodebuild command", invocationCapture.getValue(),
				commandLine(contains("xcodebuild", "-version")));
			assertThat("discards outputs", invocationCapture.getValue(), discardedOutputs());
		}
	}
}
