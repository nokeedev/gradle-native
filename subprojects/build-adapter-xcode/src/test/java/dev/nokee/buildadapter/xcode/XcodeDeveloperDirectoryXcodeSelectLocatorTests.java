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

import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectoryXcodeSelectLocator;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.CommandLineToolExecutionHandle;
import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolLogContent;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.nokee.fixtures.exec.CommandLineToolMatchers.commandLine;
import static dev.nokee.fixtures.exec.CommandLineToolMatchers.discardedOutputs;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

class XcodeDeveloperDirectoryXcodeSelectLocatorTests {
	TestDouble<CommandLineToolExecutionResult> result = newMock(CommandLineToolExecutionResult.class);
	CommandLineToolExecutionHandle.Waitable handle = () -> result.instance();
	TestDouble<CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable>> engine = newMock(CommandLineToolExecutionEngine.class)
		.when(any(callTo(method(CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable>::submit))).then(doReturn(handle)));
	XcodeDeveloperDirectoryXcodeSelectLocator subject = new XcodeDeveloperDirectoryXcodeSelectLocator(engine.instance());

	@Nested
	class DeveloperDirectory {
		/*static*/ final Path EXPECTED_DEVELOPER_DIRECTORY = Paths.get("/Applications/Xcode.app/Contents/Developer");

		@BeforeEach
		void setup() {
			result.when(callTo(method(CommandLineToolExecutionResult::getOutput)) //
				.then(doReturn(CommandLineToolLogContent.of(EXPECTED_DEVELOPER_DIRECTORY + "\n"))));
		}

		@Test
		void executesXcodeSelectWithPrintPathFlagAndDiscardedOutput() {
			assertThat("returns printed developer directory", subject.locate(),
				equalTo(EXPECTED_DEVELOPER_DIRECTORY));
			assertThat("executes xcode-select command",
				engine.to(method(CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable>::submit)),
				calledOnceWith(commandLine(contains("xcode-select", "--print-path"))));
			assertThat("discards outputs",
				engine.to(method(CommandLineToolExecutionEngine<CommandLineToolExecutionHandle.Waitable>::submit)),
				calledOnceWith(discardedOutputs()));
		}
	}
}
