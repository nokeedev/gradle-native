package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolProvider;

public class UnavailableCommandLineToolProvider implements CommandLineToolProvider {
	@Override
	public CommandLineTool get() {
		throw new IllegalArgumentException("Don't know how to provide this tool.");
	}

	@Override
	public boolean isAvailable() {
		return false;
	}
}
