package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationStandardOutputRedirectForwardImpl;
import dev.nokee.core.exec.internal.DuplicateToSystemOutputStreamCommandLineToolInvocationStandardOutputRedirect;

import java.io.Writer;

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

	/**
	 * Creates a redirection that forward the process standard output to the specified writer.
	 *
	 * @param writer a writer that will receive the process's standard output.
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance that redirect the process standard output to the specified writer, never null.
	 */
	static CommandLineToolInvocationStandardOutputRedirect forwardTo(Writer writer) {
		return new CommandLineToolInvocationStandardOutputRedirectForwardImpl(writer);
	}

	// TODO: Add factory method for appendToFile(File)
	// TODO: Add factory method for writeToFile(File) -> it will replace the file
	// TODO: Add factory method for discard() -> null all output -> open question, should it be available in the executionResult?
}
