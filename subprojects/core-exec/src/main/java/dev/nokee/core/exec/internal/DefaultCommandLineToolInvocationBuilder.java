package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.*;

import java.io.File;

public class DefaultCommandLineToolInvocationBuilder implements CommandLineToolInvocationBuilder {
	private final CommandLine commandLine;
	private boolean capturingStandardOutput = true;
	private File standardStreamFile = null;
	private File workingDirectory = null;

	public DefaultCommandLineToolInvocationBuilder(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public CommandLineToolInvocationBuilder captureStandardOutput() {
		capturingStandardOutput = true;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder workingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}

	@Override
	public CommandLineToolInvocationBuilder appendStandardStreamToFile(File file) {
		standardStreamFile = file;
		return this;
	}

	@Override
	public CommandLineToolInvocation build() {
		return new DefaultCommandLineToolInvocation(commandLine, capturingStandardOutput, standardStreamFile, workingDirectory);
	}

	@Override
	public CommandLineToolExecutionHandle buildAndSubmit(CommandLineToolExecutionEngine engine) {
		return engine.submit(build());
	}
}
