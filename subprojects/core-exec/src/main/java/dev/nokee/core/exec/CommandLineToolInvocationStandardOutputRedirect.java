package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.DuplicateToSystemOutputStreamCommandLineToolInvocationStandardOutputRedirect;

import java.io.File;

/**
 * Represents how to redirect the standard output of the command line tool invocation.
 *
 * @since 0.5
 */
public interface CommandLineToolInvocationStandardOutputRedirect {
	/**
	 * Creates a redirection that duplicate the process standard output to the {@code System#out}.
	 *
	 * @return a {@link CommandLineToolInvocationStandardOutputRedirect} instance that redirect process standard output to {@link System#out}.
	 */
	static CommandLineToolInvocationStandardOutputRedirect duplicateToSystemOutput() {
		return new DuplicateToSystemOutputStreamCommandLineToolInvocationStandardOutputRedirect();
	}

	// TODO: Add factory method for appendToFile(File)
	// TODO: Add factory method for writeToFile(File) -> it will replace the file
	// TODO: Add factory method for discard() -> null all output -> open question, should it be available in the executionResult?
}
