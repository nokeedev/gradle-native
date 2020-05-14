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

	CommandLine withArguments(Object... arguments);

	// TODO: Model the requirements of a command line tool
	//  Requirements are the runtime (host, java, python, bash, batch, etc.)
	//  Minimum version of the runtime (JDK 8, Python 2.7, bash 4.0, etc.)

	// TODO: Convenience shortcut could be provided to execute and create a newInvocation()
}
