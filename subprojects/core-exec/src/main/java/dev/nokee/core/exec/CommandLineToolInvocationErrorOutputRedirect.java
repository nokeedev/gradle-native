package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationErrorOutputRedirectForwardImpl;
import dev.nokee.core.exec.internal.CommandLineToolInvocationErrorOutputRedirectDuplicateToSystemErrorStreamImpl;

import java.io.Writer;

/**
 * Represents how to redirect the error output of the command line tool invocation.
 *
 * @since 0.5
 */
public interface CommandLineToolInvocationErrorOutputRedirect {
	/**
	 * Creates a redirection that duplicate the process error output to the {@code System#err}.
	 *
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance that redirect process error output to {@link System#err}, never null.
	 */
	static CommandLineToolInvocationErrorOutputRedirect duplicateToSystemError() {
		return new CommandLineToolInvocationErrorOutputRedirectDuplicateToSystemErrorStreamImpl();
	}

	/**
	 * Creates a redirection that forward the process error output to the specified writer.
	 *
	 * @param writer a writer that will receive the process's error output.
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance that redirect the process error output to the specified writer, never null.
	 */
	static CommandLineToolInvocationErrorOutputRedirect forwardTo(Writer writer) {
		return new CommandLineToolInvocationErrorOutputRedirectForwardImpl(writer);
	}

	// TODO: Add factory method for appendToFile(File)
	// TODO: Add factory method for writeToFile(File) -> it will replace the file
	// TODO: Add factory method for discard() -> null all output -> open question, should it be available in the executionResult?
	// TODO: Add factory method for redirectToStandardStream()
}
