package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolArguments;
import dev.nokee.core.exec.CommandLineToolInvocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Optional;

@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DefaultCommandLineToolInvocation implements CommandLineToolInvocation {
	@EqualsAndHashCode.Include private final CommandLine commandLine;
	@Getter private final boolean capturingStandardOutput;
	private final File standardStreamFile;

	@Override
	public CommandLineTool getTool() {
		return commandLine.getTool();
	}

	@Override
	public CommandLineToolArguments getArguments() {
		return commandLine.getArguments();
	}

	@Override
	public Optional<File> getStandardStreamFile() {
		return Optional.ofNullable(standardStreamFile);
	}
}
