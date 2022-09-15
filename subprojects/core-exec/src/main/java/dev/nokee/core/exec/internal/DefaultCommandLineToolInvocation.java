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

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolArguments;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables;
import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.Optional;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DefaultCommandLineToolInvocation implements CommandLineToolInvocation {
	@EqualsAndHashCode.Include private final CommandLine commandLine;
	private final CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect;
	private final CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect;
	private final File workingDirectory;
	@EqualsAndHashCode.Include private final CommandLineToolInvocationEnvironmentVariables environmentVariables;

	public DefaultCommandLineToolInvocation(CommandLine commandLine, CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect, File workingDirectory, CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		this.commandLine = commandLine;
		this.standardOutputRedirect = standardOutputRedirect;
		this.errorOutputRedirect = errorOutputRedirect;
		this.workingDirectory = workingDirectory;
		this.environmentVariables = environmentVariables;
	}

	@Override
	public CommandLineTool getTool() {
		return commandLine.getTool();
	}

	@Override
	public CommandLineToolArguments getArguments() {
		return commandLine.getArguments();
	}

	@Override
	public CommandLineToolInvocationStandardOutputRedirect getStandardOutputRedirect() {
		return standardOutputRedirect;
	}

	@Override
	public CommandLineToolInvocationErrorOutputRedirect getErrorOutputRedirect() {
		return errorOutputRedirect;
	}

	@Override
	public Optional<File> getWorkingDirectory() {
		return Optional.ofNullable(workingDirectory);
	}

	@Override
	public CommandLineToolInvocationEnvironmentVariables getEnvironmentVariables() {
		return environmentVariables;
	}
}
