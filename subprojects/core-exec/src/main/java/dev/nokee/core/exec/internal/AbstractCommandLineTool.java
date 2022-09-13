/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolArguments;
import dev.nokee.core.exec.CommandLineToolExecutableResolvable;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.CommandLineToolExecutionHandle;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.core.exec.ProcessBuilderEngine;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;

public abstract class AbstractCommandLineTool implements CommandLineTool, CommandLineToolExecutableResolvable {
	@Override
	public CommandLine withArguments(Object... arguments) {
		return new CommandLine(this, CommandLineToolArguments.of(arguments));
	}

	@Override
	public CommandLine withArguments(Iterable<?> arguments) {
		return new CommandLine(this, CommandLineToolArguments.of(ImmutableList.copyOf(arguments)));
	}

	@Override
	public CommandLineToolInvocation.Builder newInvocation() {
		return new CommandLine(this, CommandLineToolArguments.empty()).newInvocation();
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine) {
		return new CommandLine(this, CommandLineToolArguments.empty()).execute(engine);
	}

	@Override
	public ProcessBuilderEngine.Handle execute(@Nullable List<?> env, File workingDirectory) {
		return newInvocation()
			.workingDirectory(workingDirectory)
			.withEnvironmentVariables(from(env))
			.buildAndSubmit(new ProcessBuilderEngine());
	}
}
