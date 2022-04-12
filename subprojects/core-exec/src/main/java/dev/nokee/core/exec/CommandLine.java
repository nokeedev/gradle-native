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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.internal.DefaultCommandLine;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.core.exec.CommandLineUtils.getScriptCommandLine;
import static dev.nokee.utils.DeferredUtils.flatUnpack;

/**
 * A command line is composed of a tool with zero or more arguments.
 * The execution of the command can proceed in two ways:
 *
 * <ul>
 *     <li>Configure the invocation before proceeding with the execution, see {@link #newInvocation()}</li>
 *     <li>Proceed with the execution right now (provided for convenience), see {@link #execute(CommandLineToolExecutionEngine)}</li>
 * </ul>
 *
 * @since 0.4
 */
public interface CommandLine {
	/**
	 * Returns the tool of the current command line.
	 * @return a {@link CommandLineTool} instance representing the tool to execute, never null.
	 */
	CommandLineTool getTool();

	/**
	 * Returns the arguments of the current command line.
	 *
	 * @return a {@link CommandLineToolArguments} instance representing all the arguments, never null.
	 */
	CommandLineToolArguments getArguments();

	/**
	 * Prepares a new invocation via the {@link CommandLineToolInvocationBuilder}.
	 * The invocation is responsible for configuring the working directory, environment variables, standard stream manipulation, etc.
	 *
	 * @return a {@link CommandLineToolInvocationBuilder} instance, never null.
	 */
	CommandLineToolInvocationBuilder newInvocation();

	/**
	 * Convenience for {@code newInvocation().build().submit(engine)}.
	 * @param engine  the executing engine to use, cannot be null
	 * @param <T>  the execution handle type
	 * @return a {@link CommandLineToolExecutionHandle} representing the execution in progress, never null.
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

	default ProcessBuilderEngine.Handle execute(@Nullable List<?> env, Path workingDirectory) {
		return execute(env, workingDirectory.toFile());
	}

	ProcessBuilderEngine.Handle execute();

	/**
	 * Creates a {@link CommandLine} instance from the command line elements specified.
	 *
	 * @param commandLine the command line elements, cannot be empty or contains null values.
	 * @return a {@link CommandLine} instance representing the specified command line, never null.
	 */
	static CommandLine of(Object... commandLine) {
		return of(Arrays.asList(commandLine));
	}

	/**
	 * Creates a {@link CommandLine} instance from the command line elements specified.
	 *
	 * @param commandLine the command line elements, cannot be empty or contains null values.
	 * @return a {@link CommandLine} instance representing the specified command line, never null.
	 */
	static CommandLine of(@NonNull List<?> commandLine) {
		Iterator<?> it = flatUnpack(commandLine).iterator();
		Preconditions.checkArgument(it.hasNext(), "The command line must contain at least one element for the executable");
		Object executable = it.next();
		Preconditions.checkNotNull(executable, "The command line cannot contain null elements");

		ImmutableList.Builder<Object> arguments = ImmutableList.builder();
		it.forEachRemaining(element -> {
			Preconditions.checkNotNull(element, "The command line cannot contain null elements");
			arguments.add(element);
		});
		return new DefaultCommandLine(CommandLineTool.of(executable), CommandLineToolArguments.of(arguments.build()));
	}

	/**
	 * Creates a {@link CommandLine} instance using the scripting environment to execute the specified command line.
	 * The scripting environment will be {@literal cmd /c} on Windows or {@literal bash -c} on *nix.
	 *
	 * @param commandLine the command line elements, cannot be empty or contains null values.
	 * @return a {@link CommandLine} instance representing the specified command line executing in the scripting environment, never null.
	 */
	static CommandLine script(Object... commandLine) {
		return of(Arrays.asList(getScriptCommandLine(), flatUnpack(Arrays.asList(commandLine)).stream().map(Object::toString).collect(Collectors.joining(" "))));
	}
}
