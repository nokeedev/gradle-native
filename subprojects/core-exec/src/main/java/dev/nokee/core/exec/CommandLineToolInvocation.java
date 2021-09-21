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

import java.io.File;
import java.util.Optional;

/**
 * An invocation represent the runtime information for a soon to be executed tool with it's argument.
 *
 * @since 0.4
 */
public interface CommandLineToolInvocation {
	CommandLineTool getTool();

	CommandLineToolArguments getArguments();

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationEnvironmentVariables} instance representing the invocation's environment variable, never null.
	 * @since 0.5
	 */
	CommandLineToolInvocationEnvironmentVariables getEnvironmentVariables();

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance representing how to redirect the invocation's error output, never null.
	 * @since 0.5
	 */
	CommandLineToolInvocationErrorOutputRedirect getErrorOutputRedirect();

	/**
	 * Returns the environment variables to use for this command line tool invocation.
	 *
	 * @return a {@link CommandLineToolInvocationStandardOutputRedirect} instance representing how to redirect the invocation's standard output, never null.
	 * @since 0.5
	 */
	CommandLineToolInvocationStandardOutputRedirect getStandardOutputRedirect();

	Optional<File> getWorkingDirectory();
}
