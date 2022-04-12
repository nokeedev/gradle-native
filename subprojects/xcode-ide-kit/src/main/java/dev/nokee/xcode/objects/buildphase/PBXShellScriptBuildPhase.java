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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Build phase which represents running a shell script.
 */
public final class PBXShellScriptBuildPhase extends PBXBuildPhase {
    private final List<String> inputPaths;
    private final List<String> outputPaths;
    @Nullable private final String shellPath;
    @Nullable private final String shellScript;

	private PBXShellScriptBuildPhase(@Nullable String shellScript, @Nullable String shellPath, List<String> inputPaths, List<String> outputPaths) {
		super(ImmutableList.of());
		this.shellPath = shellPath;
		this.shellScript = shellScript;
		this.inputPaths = inputPaths;
		this.outputPaths = outputPaths;
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
     * Returns the list (possibly empty) of files created as output of the shell script.
     * May not be actual paths, because they can have variable interpolations.
	 *
	 * @return output paths, never null
     */
    public List<String> getOutputPaths() {
        return outputPaths;
    }

    /**
     * Returns the path to the shell under which the script is to be executed.
     * Defaults to "/bin/sh".
	 *
	 * @return shell path, may be null
     */
    @Nullable
    public String getShellPath() {
        return shellPath;
    }

    /**
     * Gets the contents of the shell script to execute under the shell
     * returned by {@link #getShellPath()}.
	 *
	 * @return shell script, may be null
     */
    @Nullable
    public String getShellScript() {
        return shellScript;
    }

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private static final String DEFAULT_SHELL_PATH = "/bin/sh";
		private static final String DEFAULT_SHELL_SCRIPT = "";

		private String shellScript = DEFAULT_SHELL_SCRIPT;
		private String shellPath = DEFAULT_SHELL_PATH;
		private final List<String> outputPaths = new ArrayList<>();
		private final List<String> inputPaths = new ArrayList<>();

		public Builder shellScript(String shellScript) {
			this.shellScript = shellScript;
			return this;
		}

		public Builder shellPath(String shellPath) {
			this.shellPath = shellPath;
			return this;
		}

		public Builder outputPaths(Iterable<String> outputPaths) {
			this.outputPaths.clear();
			outputPaths.forEach(this.outputPaths::add);
			return this;
		}

		public Builder inputPaths(Iterable<String> inputPaths) {
			this.inputPaths.clear();
			inputPaths.forEach(this.inputPaths::add);
			return this;
		}

		public PBXShellScriptBuildPhase build() {
			return new PBXShellScriptBuildPhase(shellScript, shellPath, inputPaths, outputPaths);
		}
	}
}
