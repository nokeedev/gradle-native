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
