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
package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolLogContent;
import dev.nokee.core.exec.ExecException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultCommandLineToolExecutionResult implements CommandLineToolExecutionResult {
	@Getter private final int exitValue;
	private final CommandLineToolLogContent output;
	private final CommandLineToolLogContent error;
	private final CommandLineToolLogContent fullOutput;
	private final Supplier<String> displayName;

	@Override
	public CommandLineToolLogContent getStandardOutput() {
		return output;
	}

	@Override
	public CommandLineToolLogContent getErrorOutput() {
		return error;
	}

	@Override
	public CommandLineToolLogContent getOutput() {
		return fullOutput;
	}

	@Override
	public CommandLineToolExecutionResult assertExitValueEquals(int expectedExitValue) throws ExecException {
		if (this.exitValue != expectedExitValue) {
			throw new ExecException(String.format("Process '%s' finished with unexpected exit value %d, was expecting %d", displayName.get(), exitValue, expectedExitValue));
		} else {
			return this;
		}
	}

	@Override
	public CommandLineToolExecutionResult assertNormalExitValue() throws ExecException {
		if (this.exitValue != 0) {
			throw new ExecException(String.format("Process '%s' finished with non-zero exit value %d\n%s", displayName.get(), exitValue, error.getAsString()));
		} else {
			return this;
		}
	}
}
