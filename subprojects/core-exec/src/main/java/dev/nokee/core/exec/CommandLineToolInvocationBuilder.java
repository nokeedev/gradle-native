package dev.nokee.core.exec;

import java.io.File;

/**
 * A builder for a command line invocation.
 *
 * @since 0.4
 */
public interface CommandLineToolInvocationBuilder {
	CommandLineToolInvocationBuilder captureStandardOutput();

	CommandLineToolInvocation build();

	<T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine);

	CommandLineToolInvocationBuilder appendStandardStreamToFile(File file);
}
