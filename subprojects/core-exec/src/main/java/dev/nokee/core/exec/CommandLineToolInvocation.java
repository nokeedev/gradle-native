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

import lombok.EqualsAndHashCode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static dev.nokee.core.exec.CommandLineUtils.resolve;

/**
 * An invocation represent the runtime information for a soon-to-be executed tool with its argument.
 *
 * @since 0.4
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CommandLineToolInvocation {
	@EqualsAndHashCode.Include private final CommandLineToolExecutable executable;
	@EqualsAndHashCode.Include private final CommandLineToolArguments arguments;
	private final CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect;
	private final CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect;
	private final File workingDirectory;
	@EqualsAndHashCode.Include private final CommandLineToolInvocationEnvironmentVariables environmentVariables;

	public CommandLineToolInvocation(CommandLineToolExecutable executable, CommandLineToolArguments arguments, CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect, Path workingDirectory, CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		this.executable = executable;
		this.arguments = arguments;
		this.standardOutputRedirect = standardOutputRedirect;
		this.errorOutputRedirect = errorOutputRedirect;
		this.workingDirectory = workingDirectory.toFile();
		this.environmentVariables = environmentVariables;
	}

	/**
	 * Returns the executable to use fo rthis command line tool invocation.
	 *
	 * @return the executable of this invocation, never null
	 * @since 0.5
	 */
	public CommandLineToolExecutable getExecutable() {
		return executable;
	}

	/**
	 * Returns the arguments to use for this command line tool invocation.
	 *
	 * @return the arguments of this invocation, never null
	 */
	public CommandLineToolArguments getArguments() {
		return arguments;
	}

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationEnvironmentVariables} instance representing the invocation's environment variable, never null.
	 * @since 0.5
	 */
	public CommandLineToolInvocationEnvironmentVariables getEnvironmentVariables() {
		return environmentVariables;
	}

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance representing how to redirect the invocation's error output, never null.
	 * @since 0.5
	 */
	public CommandLineToolInvocationErrorOutputRedirect getErrorOutputRedirect() {
		return errorOutputRedirect;
	}

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationStandardOutputRedirect} instance representing how to redirect the invocation's standard output, never null.
	 * @since 0.5
	 */
	public CommandLineToolInvocationStandardOutputRedirect getStandardOutputRedirect() {
		return standardOutputRedirect;
	}

	/**
	 * Returns the working directory to use for this command line tool invocation.
	 *
	 * @return the working directory of this invocation if any, never null
	 */
	public Path getWorkingDirectory() {
		return workingDirectory.toPath();
	}

	/**
	 * Submit this invocation to the specified execution engine.
	 *
	 * @param engine  the execution engine, must not be null
	 * @return the handle of this invocation execution within the specified engine, never null
	 * @param <T>  the execution handle type
	 * @since 0.5
	 */
	public <T extends CommandLineToolExecutionHandle> T submitTo(CommandLineToolExecutionEngine<T> engine) {
		Objects.requireNonNull(engine, "'engine' must not be null");
		return engine.submit(this);
	}

	/**
	 * A builder for a command line invocation.
	 *
	 * @since 0.5
	 */
	public static final class Builder {
		private CommandLine commandLine;
		private Object workingDirectory = null;
		private CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect = new CommandLineToolInvocationInheritedStandardOutputRedirection();
		private CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect = new CommandLineToolInvocationInheritedStandardErrorRedirection();
		private CommandLineToolInvocationEnvironmentVariables environmentVariables = CommandLineToolInvocationEnvironmentVariables.inherit();

		public Builder commandLine(CommandLine commandLine) {
			this.commandLine = commandLine;
			return this;
		}

		public Builder workingDirectory(Object workingDirectory) {
			this.workingDirectory = workingDirectory;
			return this;
		}

		public Builder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
			this.environmentVariables = environmentVariables;
			return this;
		}

		public CommandLineToolInvocation build() {
			final Path workingDirectory = resolveWorkingDirectory();

			Objects.requireNonNull(commandLine, "'commandLine' must not be null");

			CommandLineToolExecutable executable = ((CommandLineToolExecutableResolvable) commandLine.getTool()).resolve(new CommandLineToolExecutableResolvable.Context() {
				@Override
				public Path getWorkingDirectory() {
					return workingDirectory;
				}

				@Override
				public CommandLineToolInvocationEnvironmentVariables getEnvironmentVariables() {
					return environmentVariables;
				}
			});

			return new CommandLineToolInvocation(executable, commandLine.getArguments(), standardOutputRedirect, errorOutputRedirect, workingDirectory, environmentVariables);
		}

		private Path resolveWorkingDirectory() {
			Path result = resolve(this.workingDirectory);
			if (workingDirectory == null) {
				return Paths.get("").toAbsolutePath(); // in case Gradle configuration wrongly detect something
			} else {
				return result;
			}
		}

		public <T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine) {
			return engine.submit(build());
		}

		public Builder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect) {
			standardOutputRedirect = redirect;
			return this;
		}

		public Builder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect) {
			errorOutputRedirect = redirect;
			return this;
		}
	}
}
