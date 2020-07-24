package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolProvider;

public class FixedCommandLineToolProvider implements CommandLineToolProvider {
	private final CommandLineTool tool;

	public FixedCommandLineToolProvider(CommandLineTool tool) {
		this.tool = tool;
	}

	@Override
	public CommandLineTool get() {
		return tool;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
