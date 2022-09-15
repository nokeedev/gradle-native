/*
 * Copyright 2020-2021 the original author or authors.
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

import org.gradle.api.tasks.Internal;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * A command line tool represent a executable component that can be used as a tool.
 * The tool may be a executable, a script (batch, bash, python, etc.), executable JAR, etc.
 * The requirements of the tool will be processed by the invocation before submitting to the execution engine.
 *
 * @since 0.4
 */
public interface CommandLineTool {
	static Optional<CommandLineTool> fromPath(String executable) {
		return CommandLineTools.findInPath(executable);
	}

	static CommandLineTool of(File executable) {
		return CommandLineTools.fromLocation(executable);
	}

	static CommandLineTool of(Object executable) {
		return CommandLineTools.fromPath(executable);
	}

	@Internal
	String getExecutable();

	/**
	 * Creates a command line from this tool and the specified arguments.
	 *
	 * @param arguments the arguments forming a {@link CommandLine} with this tool.
	 * @return a {@link CommandLine} instance of this tool with the specified arguments, never null.
	 */
	CommandLine withArguments(Object... arguments);

	/**
	 * Creates a command line from this tool and the specified arguments.
	 *
	 * @param arguments the arguments forming a {@link CommandLine} with this tool.
	 * @return a {@link CommandLine} instance of this tool with the specified arguments, never null.
	 * @since 0.5
	 */
	CommandLine withArguments(Iterable<?> arguments);

	// TODO: Model the requirements of a command line tool
	//  Requirements are the runtime (host, java, python, bash, batch, etc.)
	//  Minimum version of the runtime (JDK 8, Python 2.7, bash 4.0, etc.)

	// TODO: Convenience shortcut could be provided to execute and create a newInvocation()
	/**
	 * Prepares a new invocation via the {@link CommandLineToolInvocation.Builder}.
	 * The invocation is responsible for configuring the working directory, environment variables, standard stream manipulation, etc.
	 *
	 * @return a {@link CommandLineToolInvocation.Builder} instance, never null.
	 * @since 0.5
	 */
	CommandLineToolInvocation.Builder newInvocation();

	/**
	 * Convenience for {@code newInvocation().build().submit(engine)}.
	 * @param engine  the executing engine to use, cannot be null
	 * @param <T>  the execution handle type
	 * @return a {@link CommandLineToolExecutionHandle} representing the execution in progress, never null.
	 * @since 0.5
	 */
	<T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine);

	/**
	 * Convenience for {@code newInvocation().withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables.from(env)).workingDirectory(workingDirectory).build().submit(new ProcessBuilderEngine())}.
	 * This API behave similarly to the Groovy API.
	 *
	 * @param env the environment variable to invoke the process with, null means inherited.
	 * @param workingDirectory the working directory to invoke the process in, null means inherited.
	 * @return a {@link ProcessBuilderEngine.Handle} representing the execution in progress, never null.
	 * @since 0.5
	 */
	ProcessBuilderEngine.Handle execute(@Nullable List<?> env, File workingDirectory);
}
