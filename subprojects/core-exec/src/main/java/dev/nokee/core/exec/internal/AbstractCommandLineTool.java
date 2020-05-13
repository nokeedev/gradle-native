package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;

import java.util.Arrays;

public abstract class AbstractCommandLineTool implements CommandLineTool {
	@Override
	public CommandLine withArguments(Object... arguments) {
		return new DefaultCommandLine(this, new DefaultCommandLineToolArguments(Arrays.asList(arguments)));
	}
}
