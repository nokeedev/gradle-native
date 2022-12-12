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
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;

/**
 * Build phase which represents running a shell script.
 */
public interface PBXShellScriptBuildPhase extends PBXBuildPhase {
	Optional<String> getName();

	/**
	 * Returns the list (possibly empty) of files passed as input to the shell script.
	 * May not be actual paths, because they can have variable interpolations.
	 *
	 * @return input paths, never null
	 */
	List<String> getInputPaths();

	/**
	 * Returns the list (possibly empty) of file list files, e.g. {@literal *.xcfilelist}, passed as input to the shell script.
	 *
	 * @return input file list paths, never null
	 */
	List<String> getInputFileListPaths();

	/**
	 * Returns the list (possibly empty) of files created as output of the shell script.
	 * May not be actual paths, because they can have variable interpolations.
	 *
	 * @return output paths, never null
	 */
	List<String> getOutputPaths();

	/**
	 * Returns the list (possibly empty) of file list files, e.g. {@literal *.xcfilelist}, passed as output to the shell script.
	 *
	 * @return output file list paths, never null
	 */
	List<String> getOutputFileListPaths();

	/**
	 * Returns the path to the shell under which the script is to be executed.
	 * Defaults to "/bin/sh".
	 *
	 * @return shell path, may be null
	 */
	String getShellPath();

	/**
	 * Gets the contents of the shell script to execute under the shell
	 * returned by {@link #getShellPath()}.
	 *
	 * @return shell script, may be null
	 */
	String getShellScript();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXShellScriptBuildPhase>, LenientAwareBuilder<Builder> {
		private static final String DEFAULT_SHELL_PATH = "/bin/sh";
		private static final String DEFAULT_SHELL_SCRIPT = "";

		private String name;
		private String shellScript;
		private String shellPath;
		private final List<String> outputPaths = new ArrayList<>();
		private final List<String> inputPaths = new ArrayList<>();
		private final List<String> inputFileListPaths = new ArrayList<>();
		private final List<String> outputFileListPaths = new ArrayList<>();
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXShellScriptBuildPhase");
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

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

		public Builder outputPath(String outputPath) {
			Objects.requireNonNull(outputPath);
			outputPaths.add(outputPath);
			return this;
		}

		public Builder inputPaths(Iterable<String> inputPaths) {
			this.inputPaths.clear();
			stream(inputPaths).map(Objects::requireNonNull).forEach(this.inputPaths::add);
			return this;
		}

		public Builder inputPath(String inputPath) {
			Objects.requireNonNull(inputPath);
			inputPaths.add(inputPath);
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

		@Override
		public PBXShellScriptBuildPhase build() {
			if (shellScript == null) {
				shellScript = DEFAULT_SHELL_SCRIPT;
			}

			if (shellPath == null) {
				shellPath = DEFAULT_SHELL_PATH;
			}

			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.name, name);
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, shellScript);
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, shellPath);
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, ImmutableList.copyOf(inputPaths));
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, ImmutableList.copyOf(inputFileListPaths));
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, ImmutableList.copyOf(outputPaths));
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, ImmutableList.copyOf(outputFileListPaths));

			return new CodeablePBXShellScriptBuildPhase(builder.build());
		}
	}
}
