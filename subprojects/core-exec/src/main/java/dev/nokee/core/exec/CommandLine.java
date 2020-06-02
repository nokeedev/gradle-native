package dev.nokee.core.exec;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.internal.DefaultCommandLine;
import dev.nokee.core.exec.internal.DefaultCommandLineToolArguments;
import dev.nokee.core.exec.internal.SystemCommandLineTool;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
	 * @param engine the executing engine to use, cannot be null
	 * @return a {@link CommandLineToolExecutionHandle} representing the execution in progress, never null.
	 */
	<T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine);

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
	static CommandLine of(@NonNull List<Object> commandLine) {
		Iterator<Object> it = commandLine.iterator();
		Preconditions.checkArgument(it.hasNext(), "The command line must contain at least one element for the executable");
		Object executable = it.next();
		Preconditions.checkNotNull(executable, "The command line cannot contain null elements");

		ImmutableList.Builder<Object> arguments = ImmutableList.builder();
		it.forEachRemaining(element -> {
			Preconditions.checkNotNull(element, "The command line cannot contain null elements");
			arguments.add(element);
		});
		return new DefaultCommandLine(new SystemCommandLineTool(executable), new DefaultCommandLineToolArguments(arguments.build()));
	}
}
