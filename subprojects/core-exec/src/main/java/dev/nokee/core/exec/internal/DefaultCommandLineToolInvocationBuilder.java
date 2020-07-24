package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.*;

import java.io.File;

public class DefaultCommandLineToolInvocationBuilder implements CommandLineToolInvocationBuilder {
	private final CommandLine commandLine;
	private File workingDirectory = null;
	private CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect = new InheritCommandLineToolInvocationOutputRedirect();
	private CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect = new InheritCommandLineToolInvocationOutputRedirect();
	private CommandLineToolInvocationEnvironmentVariables environmentVariables = new InheritCommandLineToolInvocationEnvironmentVariables();

	public DefaultCommandLineToolInvocationBuilder(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public CommandLineToolInvocationBuilder workingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		this.environmentVariables = environmentVariables;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder appendStandardStreamToFile(File file) {
		standardOutputRedirect = new AppendStandardStreamToFileCommandLineToolInvocationOutputRedirect(file);
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect) {
		standardOutputRedirect = redirect;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect) {
		errorOutputRedirect = redirect;
		return this;
	}

	@Override
	public CommandLineToolInvocation build() {
		return new DefaultCommandLineToolInvocation(commandLine, standardOutputRedirect, errorOutputRedirect, workingDirectory, environmentVariables);
	}

	@Override
	public CommandLineToolExecutionHandle buildAndSubmit(CommandLineToolExecutionEngine engine) {
		return engine.submit(build());
	}
}
