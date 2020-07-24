package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.SystemCommandLineTool;
import org.gradle.api.tasks.Internal;

import java.io.File;

/**
 * A command line tool represent a executable component that can be used as a tool.
 * The tool may be a executable, a script (batch, bash, python, etc.), executable JAR, etc.
 * The requirements of the tool will be processed by the invocation before submitting to the execution engine.
 *
 * @since 0.4
 */
public interface CommandLineTool {
	static CommandLineTool fromPath(String executable) {
		return CommandLineToolFactory.fromPath(executable);
	}

	static CommandLineTool of(File executable) {
		return CommandLineToolFactory.fromLocation(executable);
	}

	static CommandLineTool of(Object executable) {
		return new SystemCommandLineTool(executable);
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
	 * Prepares a new invocation via the {@link CommandLineToolInvocationBuilder}.
	 * The invocation is responsible for configuring the working directory, environment variables, standard stream manipulation, etc.
	 *
	 * @return a {@link CommandLineToolInvocationBuilder} instance, never null.
	 * @since 0.5
	 */
	CommandLineToolInvocationBuilder newInvocation();

	/**
	 * Convenience for {@code newInvocation().build().submit(engine)}.
	 * @param engine the executing engine to use, cannot be null
	 * @return a {@link CommandLineToolExecutionHandle} representing the execution in progress, never null.
	 * @since 0.5
	 */
	<T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine);
}
