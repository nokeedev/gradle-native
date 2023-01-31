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

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.internal.DefaultCommandLineToolExecutionResult;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.nokee.core.exec.CommandLineToolOutputStreams.execute;

@EqualsAndHashCode
public class ProcessBuilderEngine implements CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> {
	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(ImmutableList.<String>builder().add(invocation.getExecutable().getLocation().toString()).addAll(invocation.getArguments()).build());
		processBuilder.directory(invocation.getWorkingDirectory().toFile());
		processBuilder.environment().clear();
		processBuilder.environment().putAll(invocation.getEnvironmentVariables().getAsMap());

		val result = execute(invocation, (outStream, errStream) -> {
			try {
				Process process = processBuilder.start();

				PumpStreamHandler streamHandler = new PumpStreamHandler(outStream, errStream);
				streamHandler.setProcessOutputStream(process.getInputStream());
				streamHandler.setProcessErrorStream(process.getErrorStream());
				streamHandler.start();
				return new ProcessResult(process, streamHandler);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});

		return new Handle(result.getResult().process, result.getResult().streamHandler, result::getStandardOutput, result::getErrorOutput, result::getOutput, () -> String.join(" ", processBuilder.command()), () -> IOUtils.closeQuietly(result));
	}

	private static final class ProcessResult {

		private final Process process;
		private final PumpStreamHandler streamHandler;

		public ProcessResult(Process process, PumpStreamHandler streamHandler) {
			this.process = process;
			this.streamHandler = streamHandler;
		}
	}

	@RequiredArgsConstructor
	public static class Handle implements CommandLineToolExecutionHandle.Waitable {
		private final Process process;
		private final PumpStreamHandler streamHandler;
		private final Supplier<CommandLineToolLogContent> standardOutput;
		private final Supplier<CommandLineToolLogContent> errorOutput;
		private final Supplier<CommandLineToolLogContent> output;
		private final Supplier<String> displayName;
		private final Runnable close;

		public CommandLineToolExecutionResult waitFor() {
			try {
				process.waitFor();
				streamHandler.stop();
				close.run();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		public CommandLineToolExecutionResult waitFor(long timeout, TimeUnit unit) {
			try {
				process.waitFor(timeout, unit);
				streamHandler.stop();
				close.run();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		public CommandLineToolExecutionResult waitFor(Duration duration) {
			try {
				process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS);
				streamHandler.stop();
				close.run();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
