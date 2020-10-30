package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationStandardOutputRedirectDuplicateToSystemOutputStreamImpl;
import dev.nokee.core.exec.internal.CommandLineToolInvocationStandardOutputRedirectForwardImpl;

import java.io.OutputStream;

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
		return new CommandLineToolInvocationStandardOutputRedirectDuplicateToSystemOutputStreamImpl();
	}

	/**
	 * Creates a redirection that forward the process standard output to the specified writer.
	 *
	 * @param outputStream a output stream that will receive the process's standard output.
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance that redirect the process standard output to the specified output stream, never null.
	 */
	static CommandLineToolInvocationStandardOutputRedirect forwardTo(OutputStream outputStream) {
		return new CommandLineToolInvocationStandardOutputRedirectForwardImpl(outputStream);
	}

	// TODO: Add factory method for appendToFile(File)
	// TODO: Add factory method for writeToFile(File) -> it will replace the file
	// TODO: Add factory method for discard() -> null all output -> open question, should it be available in the executionResult?
}
