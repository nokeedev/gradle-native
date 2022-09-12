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
package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.DefaultCommandLineToolInvocation;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Paths;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect.redirectToStandardStream;
import static dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect.duplicateToSystemOutput;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCommandLineToolInvocationTests {
	CommandLineToolInvocation subject = new DefaultCommandLineToolInvocation(CommandLine.of("my-tool", "arg1", "arg2"), duplicateToSystemOutput(), redirectToStandardStream(), Paths.get("my", "working", "directory").toFile(), inherit());

	@Test
	void hasCommandLineTool() {
		assertThat(subject.getTool(), notNullValue()); // TODO: check actual value
	}

	@Test
	void hasArguments() {
		assertThat(subject.getArguments(), equalTo(CommandLineToolArguments.of("arg1", "arg2")));
	}

	@Test
	void hasStandardOutputRedirect() {
		assertThat(subject.getStandardOutputRedirect(), notNullValue()); // TODO: check actual value
	}

	@Test
	void hasErrorOutputRedirect() {
		assertThat(subject.getErrorOutputRedirect(), notNullValue()); // TODO: check actual value
	}

	@Test
	void hasWorkingDirectory() {
		assertThat(subject.getWorkingDirectory(), optionalWithValue(withAbsolutePath(endsWith("/my/working/directory"))));
	}

	@Test
	void hasEnvironmentVariables() {
		assertThat(subject.getEnvironmentVariables(), equalTo(inherit()));
	}

	@Test
	void canSubmitToExecutionEngine() {
		val handle = Mockito.mock(CommandLineToolExecutionHandle.class);
		val engine = new CommandLineToolExecutionEngine<CommandLineToolExecutionHandle>() {
			@Override
			public CommandLineToolExecutionHandle submit(CommandLineToolInvocation invocation) {
				assertThat(invocation, equalTo(subject));
				return handle;
			}
		};
		assertThat(subject.submitTo(engine), equalTo(handle));
	}

	@Test
	void throwsExceptionWhenEngineIsNull() {
		assertThrows(NullPointerException.class, () -> subject.submitTo(null));
	}
}
