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

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Paths;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineToolInvocationTests {
	CommandLineToolInvocation subject = new CommandLineToolInvocation(new CommandLineToolExecutable(Paths.get("my-tool")), new CommandLineToolArguments(ImmutableList.of("arg1", "arg2")), new CommandLineToolInvocationOutputRedirection.ToSystemOutputRedirection(), new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection(), Paths.get("my", "working", "directory"), inherit());

	@Test
	void hasExecutable() {
		assertThat(subject.getExecutable(), equalTo(new CommandLineToolExecutable(Paths.get("my-tool"))));
	}

	@Test
	void hasArguments() {
		assertThat(subject.getArguments(), equalTo(new CommandLineToolArguments(ImmutableList.of("arg1", "arg2"))));
	}

	@Test
	void hasStandardOutputRedirect() {
		assertThat(subject.getStandardOutputRedirect(), equalTo(new CommandLineToolInvocationOutputRedirection.ToSystemOutputRedirection()));
	}

	@Test
	void hasErrorOutputRedirect() {
		assertThat(subject.getErrorOutputRedirect(), equalTo(new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection()));
	}

	@Test
	void hasWorkingDirectory() {
		assertThat(subject.getWorkingDirectory(), aFile(withAbsolutePath(endsWith("/my/working/directory"))));
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
