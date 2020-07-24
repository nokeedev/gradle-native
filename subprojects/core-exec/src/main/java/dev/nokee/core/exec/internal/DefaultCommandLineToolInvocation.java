package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Optional;

@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DefaultCommandLineToolInvocation implements CommandLineToolInvocation {
	@EqualsAndHashCode.Include private final CommandLine commandLine;
	@Getter private final CommandLineToolInvocationStandardOutputRedirect standardOutputRedirect;
	@Getter private final CommandLineToolInvocationErrorOutputRedirect errorOutputRedirect;
	private final File workingDirectory;
	@Getter @EqualsAndHashCode.Include private final CommandLineToolInvocationEnvironmentVariables environmentVariables;

	@Override
	public CommandLineTool getTool() {
		return commandLine.getTool();
	}

	@Override
	public CommandLineToolArguments getArguments() {
		return commandLine.getArguments();
	}

	@Override
	public Optional<File> getWorkingDirectory() {
		return Optional.ofNullable(workingDirectory);
	}
}
