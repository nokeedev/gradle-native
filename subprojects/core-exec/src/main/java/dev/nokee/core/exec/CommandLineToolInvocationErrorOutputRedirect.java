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

import dev.nokee.core.exec.internal.CommandLineToolInvocationErrorOutputRedirectDuplicateToSystemErrorStreamImpl;
import dev.nokee.core.exec.internal.CommandLineToolInvocationErrorOutputRedirectForwardImpl;

import java.io.OutputStream;

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
	 * @param outputStream a output stream that will receive the process's error output.
	 * @return a {@link CommandLineToolInvocationErrorOutputRedirect} instance that redirect the process error output to the specified output stream, never null.
	 */
	static CommandLineToolInvocationErrorOutputRedirect forwardTo(OutputStream outputStream) {
		return new CommandLineToolInvocationErrorOutputRedirectForwardImpl(outputStream);
	}

	// TODO: Add factory method for appendToFile(File)
	// TODO: Add factory method for writeToFile(File) -> it will replace the file
	// TODO: Add factory method for discard() -> null all output -> open question, should it be available in the executionResult?
	// TODO: Add factory method for redirectToStandardStream()
}
