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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.CommandLineToolExecutionHandle;
import dev.nokee.core.exec.CommandLineToolOutputParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DefaultXcodebuild implements Xcodebuild {
	private final CommandLineToolExecutionEngine<? extends CommandLineToolExecutionHandle.Waitable> engine;

	public DefaultXcodebuild(CommandLineToolExecutionEngine<? extends CommandLineToolExecutionHandle.Waitable> engine) {
		this.engine = engine;
	}

	@Override
	public String version() {
		return CommandLineTool.of("xcodebuild").withArguments("-version").execute(engine).waitFor().getOutput().parse(asXcodeRunVersion());
	}

	private static final Pattern XCODEBUILD_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?)");
	private static CommandLineToolOutputParser<String> asXcodeRunVersion() {
		return content -> {
			Matcher matcher = XCODEBUILD_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return matcher.group(1);
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}
}
