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
package dev.nokee.core.exec;

import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.OutputStream;

@SuppressWarnings("UnstableApiUsage")
public final class ExecOperationsExecutionEngine implements CommandLineToolExecutionEngine<ExecOperationsExecutionEngine.Handle> {
	private final ExecOperations execOperations;

	public ExecOperationsExecutionEngine(ExecOperations execOperations) {
		this.execOperations = execOperations;
	}

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		val result = CommandLineToolOutputStreams.execute(invocation, (outStream, errStream) -> execOperations.exec(ActionUtils.composite(new InvocationToExecSpec(invocation), this::defaultValues, setOutputStreams(outStream, errStream))));
		return new Handle(result.getResult(), invocation, result.getStandardOutput(), result.getErrorOutput(), result.getOutput());
	}

	private void defaultValues(ExecSpec spec) {
		spec.setIgnoreExitValue(true);
	}

	private Action<ExecSpec> setOutputStreams(OutputStream stdout, OutputStream stderr) {
		return spec -> {
			spec.setStandardOutput(stdout);
			spec.setErrorOutput(stderr);
		};
	}

	public static class Handle implements CommandLineToolExecutionHandle {
		private final ExecResult result;
		private final CommandLineToolInvocation invocation;
		private final String displayName;
		private final CommandLineToolLogContent output;
		private final CommandLineToolLogContent error;
		private final CommandLineToolLogContent fullOutput;

		public Handle(ExecResult result, CommandLineToolInvocation invocation, CommandLineToolLogContent output, CommandLineToolLogContent error, CommandLineToolLogContent fullOutput) {
			this.result = result;
			this.invocation = invocation;
			this.displayName = invocation.getExecutable().getLocation() + " " + String.join(" ", invocation.getArguments());
			this.output = output;
			this.error = error;
			this.fullOutput = fullOutput;
		}

		public CommandLineToolExecutionResult result() {
			return new CommandLineToolExecutionResult() {
				private final int exitValue = result.getExitValue();

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
				public int getExitValue() {
					return result.getExitValue();
				}

				@Override
				public CommandLineToolExecutionResult assertNormalExitValue() throws ExecException {
					if (this.exitValue != 0) {
						if (invocation.getErrorOutputRedirect() instanceof CommandLineToolInvocationOutputRedirection.ToStandardStreamRedirection && invocation.getStandardOutputRedirect() instanceof CommandLineToolInvocationOutputRedirection.ToFileRedirection) {
							throw new ExecException(String.format("Process '%s' finished with non-zero exit value %d, see %s for more information.", displayName, result.getExitValue(), new ConsoleRenderer().asClickableFileUrl(((CommandLineToolInvocationOutputRedirection.ToFileRedirection) invocation.getStandardOutputRedirect()).getOutputFile())));
						} else {
							throw new ExecException(String.format("Process '%s' finished with non-zero exit value %d\n%s", displayName, exitValue, error.getAsString()));
						}
					} else {
						return this;
					}
				}

				@Override
				public CommandLineToolExecutionResult assertExitValueEquals(int expectedExitValue) throws ExecException {
					if (this.exitValue != expectedExitValue) {
						throw new ExecException(String.format("Process '%s' finished with unexpected exit value %d, was expecting %d", displayName, exitValue, expectedExitValue));
					} else {
						return this;
					}
				}
			};
		}
	}
}
