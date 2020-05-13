package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.DefaultCommandLineTool;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public interface CommandLineToolFactory {
	public static CommandLineTool fromPath(String executable) {
		return new DefaultCommandLineTool(OperatingSystem.current().findInPath(executable));
	}

	public static CommandLineTool fromLocation(File executable) {
		return new DefaultCommandLineTool(executable);
	}
}
