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

	boolean isCapturingStandardOutput();

	Optional<File> getStandardStreamFile();
}
