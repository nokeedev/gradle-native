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
package dev.gradleplugins.exemplarkit.asciidoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class AsciidocCommandLineParser {
	private final String executable;
	private final List<String> arguments;

	private AsciidocCommandLineParser(String executable, List<String> arguments) {
		this.executable = executable;
		this.arguments = arguments;
	}

	public String getExecutable() {
		return executable;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public static AsciidocCommandLineParser parse(String commandLine) {
		String[] commandLineWords = commandLine.split("\\s+");
		String executable = commandLineWords[0];

		List<String> arguments = Collections.emptyList();
		if (commandLineWords.length > 1) {
			arguments = Arrays.asList(Arrays.copyOfRange(commandLineWords, 1, commandLineWords.length));
		}

		return new AsciidocCommandLineParser(executable, arguments);
	}
}
