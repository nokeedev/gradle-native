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

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.CommandLineToolExecutionHandle;
import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toNullStream;

public final class DefaultXcodeSelect implements XcodeSelect {
	private final CommandLineToolExecutionEngine<? extends CommandLineToolExecutionHandle.Waitable> engine;

	public DefaultXcodeSelect(CommandLineToolExecutionEngine<? extends CommandLineToolExecutionHandle.Waitable> engine) {
		this.engine = engine;
	}

	@Override
	public Path developerDirectory() {
		return CommandLine.of("xcode-select", "--print-path").newInvocation().redirectStandardOutput(toNullStream()).redirectErrorOutput(toNullStream()).buildAndSubmit(engine).waitFor().getOutput()
			.parse(this::parsePrintedPath);
	}

	private Path parsePrintedPath(String output) {
		return Paths.get(output.trim());
	}
}
