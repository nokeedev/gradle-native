/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;

/**
 * Build phase which represents running a shell script.
 */
public final class PBXShellScriptBuildPhase extends PBXBuildPhase {
	private static final ImmutableList<PBXBuildFile> ALWAYS_HAS_EMPTY_FILES = ImmutableList.of();
	@Nullable private final String name;
	private final List<String> inputPaths;
	private final List<String> inputFileListPaths;
	private final List<String> outputPaths;
	private final List<String> outputFileListPaths;
	private final String shellPath;
	private final String shellScript;

	private PBXShellScriptBuildPhase(@Nullable String name, String shellScript, String shellPath, ImmutableList<String> inputPaths, ImmutableList<String> inputFileListPaths, ImmutableList<String> outputPaths, ImmutableList<String> outputFileListPaths) {
		super(ALWAYS_HAS_EMPTY_FILES);
		this.name = name;
		this.shellPath = shellPath;
		this.shellScript = shellScript;
		this.inputPaths = inputPaths;
		this.inputFileListPaths = inputFileListPaths;
		this.outputPaths = outputPaths;
		this.outputFileListPaths = outputFileListPaths;
	}

	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	/**
	 * Returns the list (possibly empty) of files passed as input to the shell script.
	 * May not be actual paths, because they can have variable interpolations.
	 *
	 * @return input paths, never null
	 */
	public List<String> getInputPaths() {
		return inputPaths;
	}

	/**
	 * Returns the list (possibly empty) of file list files, e.g. {@literal *.xcfilelist}, passed as input to the shell script.
	 *
	 * @return input file list paths, never null
	 */
	public List<String> getInputFileListPaths() {
		return inputFileListPaths;
	}

	/**
	 * Returns the list (possibly empty) of files created as output of the shell script.
	 * May not be actual paths, because they can have variable interpolations.
	 *
	 * @return output paths, never null
	 */
	public List<String> getOutputPaths() {
		return outputPaths;
	}

	/**
	 * Returns the list (possibly empty) of file list files, e.g. {@literal *.xcfilelist}, passed as output to the shell script.
	 *
	 * @return output file list paths, never null
	 */
	public List<String> getOutputFileListPaths() {
		return outputFileListPaths;
	}

	/**
	 * Returns the path to the shell under which the script is to be executed.
	 * Defaults to "/bin/sh".
	 *
	 * @return shell path, may be null
	 */
	public String getShellPath() {
		return shellPath;
	}

	/**
	 * Gets the contents of the shell script to execute under the shell
	 * returned by {@link #getShellPath()}.
	 *
	 * @return shell script, may be null
	 */
	public String getShellScript() {
		return shellScript;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private static final String DEFAULT_SHELL_PATH = "/bin/sh";
		private static final String DEFAULT_SHELL_SCRIPT = "";

		private String name;
		private String shellScript;
		private String shellPath;
		private final List<String> outputPaths = new ArrayList<>();
		private final List<String> inputPaths = new ArrayList<>();
		private final List<String> inputFileListPaths = new ArrayList<>();
		private final List<String> outputFileListPaths = new ArrayList<>();

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder shellScript(String shellScript) {
			this.shellScript = Objects.requireNonNull(shellScript);
			return this;
		}

		public Builder shellPath(String shellPath) {
			this.shellPath = Objects.requireNonNull(shellPath);
			return this;
		}

		public Builder outputPaths(Iterable<String> outputPaths) {
			this.outputPaths.clear();
			stream(outputPaths).map(Objects::requireNonNull).forEach(this.outputPaths::add);
			return this;
		}

		public Builder inputPaths(Iterable<String> inputPaths) {
			this.inputPaths.clear();
			stream(inputPaths).map(Objects::requireNonNull).forEach(this.inputPaths::add);
			return this;
		}

		public Builder inputFileListPaths(Iterable<String> inputFileListPaths) {
			this.inputFileListPaths.clear();
			stream(inputFileListPaths).map(Objects::requireNonNull).forEach(this.inputFileListPaths::add);
			return this;
		}

		public Builder outputFileListPaths(Iterable<String> outputFileListPaths) {
			this.outputFileListPaths.clear();
			stream(outputFileListPaths).map(Objects::requireNonNull).forEach(this.outputFileListPaths::add);
			return this;
		}

		public PBXShellScriptBuildPhase build() {
			if (shellScript == null) {
				shellScript = DEFAULT_SHELL_SCRIPT;
			}

			if (shellPath == null) {
				shellPath = DEFAULT_SHELL_PATH;
			}

			return new PBXShellScriptBuildPhase(name, shellScript, shellPath, ImmutableList.copyOf(inputPaths), ImmutableList.copyOf(inputFileListPaths), ImmutableList.copyOf(outputPaths), ImmutableList.copyOf(outputFileListPaths));
		}
	}
}
