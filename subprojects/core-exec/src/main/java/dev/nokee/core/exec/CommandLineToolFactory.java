package dev.nokee.core.exec;

import java.io.File;

public interface CommandLineToolFactory {
	public static CommandLineTool fromPath(String executable) {
		return CommandLineTools.findInPath(executable);
	}

	public static CommandLineTool fromLocation(File executable) {
		return CommandLineTools.fromLocation(executable);
	}
}
