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

import dev.nokee.core.exec.*;
import lombok.EqualsAndHashCode;

import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;

@EqualsAndHashCode
public class DefaultCommandLine implements CommandLine {
	private final CommandLineTool tool;
	private final CommandLineToolArguments arguments;

	public DefaultCommandLine(CommandLineTool tool, CommandLineToolArguments arguments) {
		this.tool = tool;
		this.arguments = arguments;
	}

	@Override
	public CommandLineTool getTool() {
		return tool;
	}

	@Override
	public CommandLineToolArguments getArguments() {
		return arguments;
	}

	@Override
	public CommandLineToolInvocationBuilder newInvocation() {
		return new DefaultCommandLineToolInvocationBuilder(this);
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine) {
		return newInvocation().buildAndSubmit(engine);
	}

	public ProcessBuilderEngine.Handle execute(List<?> env, Object workingDirectory) {
		return newInvocation()
			.workingDirectory(workingDirectory)
			.withEnvironmentVariables(from(env))
			.buildAndSubmit(new ProcessBuilderEngine());
	}

	@Override
	public ProcessBuilderEngine.Handle execute() {
		return execute(new ProcessBuilderEngine());
	}
}
