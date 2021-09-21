/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.AbstractCommandLineTool;
import org.gradle.api.tasks.*;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.util.Optional;

final class CommandLineTools {
	private CommandLineTools() {}

	public static Optional<CommandLineTool> findInPath(String executable) {
		// TODO: Write integration test for this
		return Optional.ofNullable(OperatingSystem.current().findInPath(executable)).map(CommandLineTools::fromLocation);
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
