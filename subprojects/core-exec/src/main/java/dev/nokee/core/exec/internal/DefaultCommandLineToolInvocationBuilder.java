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

import java.io.File;

public class DefaultCommandLineToolInvocationBuilder implements CommandLineToolInvocationBuilder {
	private final CommandLine commandLine;
	private File workingDirectory = null;
	private CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect = new CommandLineToolInvocationOutputRedirectInheritImpl();
	private CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect = new CommandLineToolInvocationOutputRedirectInheritImpl();
	private CommandLineToolInvocationEnvironmentVariables environmentVariables = CommandLineToolInvocationEnvironmentVariables.inherit();

	public DefaultCommandLineToolInvocationBuilder(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public CommandLineToolInvocationBuilder workingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		this.environmentVariables = environmentVariables;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder appendStandardStreamToFile(File file) {
		standardOutputRedirect = new CommandLineToolInvocationStandardOutputRedirectAppendToFileImpl(file);
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect) {
		standardOutputRedirect = redirect;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect) {
		errorOutputRedirect = redirect;
		return this;
	}

	@Override
	public CommandLineToolInvocation build() {
		return new DefaultCommandLineToolInvocation(commandLine, standardOutputRedirect, errorOutputRedirect, workingDirectory, environmentVariables);
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine) {
		return engine.submit(build());
	}
}
