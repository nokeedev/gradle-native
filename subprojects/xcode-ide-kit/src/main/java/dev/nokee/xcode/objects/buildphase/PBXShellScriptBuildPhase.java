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

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

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

		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXShellScriptBuildPhase");

			// Default values
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, DEFAULT_SHELL_SCRIPT);
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, DEFAULT_SHELL_PATH);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.name, requireNonNull(name));
			return this;
		}

		public Builder shellScript(String shellScript) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, requireNonNull(shellScript));
			return this;
		}

		public Builder shellPath(String shellPath) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, requireNonNull(shellPath));
			return this;
		}

		public Builder outputPaths(Iterable<String> outputPaths) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, ImmutableList.copyOf(outputPaths));
			return this;
		}

		public Builder outputPath(String outputPath) {
			builder.add(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, outputPath);
			return this;
		}

		public Builder inputPaths(Iterable<String> inputPaths) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, ImmutableList.copyOf(inputPaths));
			return this;
		}

		public Builder inputPath(String inputPath) {
			builder.add(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, inputPath);
			return this;
		}

		public Builder inputFileListPaths(Iterable<String> inputFileListPaths) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, ImmutableList.copyOf(inputFileListPaths));
			return this;
		}

		public Builder outputFileListPaths(Iterable<String> outputFileListPaths) {
			builder.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, ImmutableList.copyOf(outputFileListPaths));
			return this;
		}

		@Override
		public PBXShellScriptBuildPhase build() {
			return new CodeablePBXShellScriptBuildPhase(builder.build());
		}
	}
}
