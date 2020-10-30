package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.AbstractCommandLineTool;
import org.gradle.api.tasks.*;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;

final class CommandLineTools {
	private CommandLineTools() {}

	public static CommandLineTool findInPath(String executable) {
		// TODO: Write integration test for this
		return new CommandLineToolFromFileSystemLocationImpl(OperatingSystem.current().findInPath(executable));
	}

	public static CommandLineTool fromPath(Object executable) {
		return new CommandLineToolFromPathAtInvocationImpl(executable);
	}

	public static CommandLineTool fromLocation(File executable) {
		return new CommandLineToolFromFileSystemLocationImpl(executable);
	}

	/**
	 * Up-to-date checking should use the executable name and the value of the PATH environment variable.
	 * Unfortunately, the PATH environment variable depends on the invocation configuration which isn't captured here.
	 * TODO: We should consider providing a builder that can configure a PATH provider to properly calculate the input value.
	 */
	private static final class CommandLineToolFromPathAtInvocationImpl extends AbstractCommandLineTool {
		private final Object executable;

		private CommandLineToolFromPathAtInvocationImpl(Object executable) {
			this.executable = executable;
		}

		@Input
		@Override
		public String getExecutable() {
			return executable.toString();
		}
	}

	/**
	 * Up-to-date checking uses the hash of the executable file.
	 * If the executable file depends on other files included with the tools, those needs to be added to the tasks input values.
	 * TODO: We should consider providing a builder that can configure those added files and/or environment variables that affect the execution.
	 */
	private static final class CommandLineToolFromFileSystemLocationImpl extends AbstractCommandLineTool {
		private final File toolLocation;

		private CommandLineToolFromFileSystemLocationImpl(File toolLocation) {
			this.toolLocation = toolLocation;
		}

		@Internal
		@Override
		public String getExecutable() {
			return toolLocation.getAbsolutePath();
		}

		@InputFile
		@PathSensitive(PathSensitivity.NONE)
		protected File getInputFile() {
			return toolLocation;
		}
	}
}
