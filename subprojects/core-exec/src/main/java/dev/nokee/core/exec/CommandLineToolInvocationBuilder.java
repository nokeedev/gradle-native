package dev.nokee.core.exec;

import java.io.File;

/**
 * A builder for a command line invocation.
 *
 * @since 0.4
 */
public interface CommandLineToolInvocationBuilder {
	// TODO: Support Provider
	// TODO: Support Directory
	CommandLineToolInvocationBuilder workingDirectory(File workingDirectory);

	CommandLineToolInvocationBuilder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables);

	CommandLineToolInvocation build();

	<T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine);

	CommandLineToolInvocationBuilder appendStandardStreamToFile(File file);

	CommandLineToolInvocationBuilder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect);

	CommandLineToolInvocationBuilder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect);
}
