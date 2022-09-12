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
package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationOutputRedirectInheritImpl;
import dev.nokee.core.exec.internal.CommandLineToolInvocationStandardOutputRedirectAppendToFileImpl;

import java.io.File;

import static dev.nokee.core.exec.CommandLineUtils.resolve;

/**
 * A builder for a command line invocation.
 *
 * @since 0.4
 */
public final class CommandLineToolInvocationBuilder {
	private final CommandLine commandLine;
	private Object workingDirectory = null;
	private CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect = new CommandLineToolInvocationOutputRedirectInheritImpl();
	private CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect = new CommandLineToolInvocationOutputRedirectInheritImpl();
	private CommandLineToolInvocationEnvironmentVariables environmentVariables = CommandLineToolInvocationEnvironmentVariables.inherit();

	public CommandLineToolInvocationBuilder(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	public CommandLineToolInvocationBuilder workingDirectory(Object workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}

	public CommandLineToolInvocationBuilder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		this.environmentVariables = environmentVariables;
		return this;
	}

	public CommandLineToolInvocation build() {
		return new CommandLineToolInvocation(commandLine, standardOutputRedirect, errorOutputRedirect, resolve(workingDirectory), environmentVariables);
	}

	public <T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine) {
		return engine.submit(build());
	}

	public CommandLineToolInvocationBuilder appendStandardStreamToFile(File file) {
		standardOutputRedirect = new CommandLineToolInvocationStandardOutputRedirectAppendToFileImpl(file);
		return this;
	}

	public CommandLineToolInvocationBuilder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect) {
		standardOutputRedirect = redirect;
		return this;
	}

	public CommandLineToolInvocationBuilder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect) {
		errorOutputRedirect = redirect;
		return this;
	}
}
