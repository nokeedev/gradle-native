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

import dev.nokee.core.exec.internal.CommandLineToolInvocationOutputRedirectInternal;
import dev.nokee.core.exec.internal.CommandLineToolOutputStreams;
import dev.nokee.core.exec.internal.CommandLineToolOutputStreamsIntertwineImpl;
import dev.nokee.core.exec.internal.DefaultCommandLineToolExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ProcessBuilderEngine implements CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> {
	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command().add(invocation.getTool().getExecutable());
		processBuilder.command().addAll(invocation.getArguments().get());
		invocation.getWorkingDirectory().ifPresent(processBuilder::directory);
		processBuilder.environment().putAll(invocation.getEnvironmentVariables().getAsMap());
		try {
			Process process = processBuilder.start();

			val endStreams = new CommandLineToolOutputStreamsIntertwineImpl();
			CommandLineToolOutputStreams streams = endStreams;
			if (invocation.getStandardOutputRedirect() instanceof CommandLineToolInvocationOutputRedirectInternal) {
				streams = ((CommandLineToolInvocationOutputRedirectInternal) invocation.getStandardOutputRedirect()).redirect(streams);
			}
			if (invocation.getErrorOutputRedirect() instanceof CommandLineToolInvocationOutputRedirectInternal) {
				streams = ((CommandLineToolInvocationOutputRedirectInternal) invocation.getErrorOutputRedirect()).redirect(streams);
			}

			PumpStreamHandler streamHandler = new PumpStreamHandler(streams.getStandardOutput(), streams.getErrorOutput());
			streamHandler.setProcessOutputStream(process.getInputStream());
			streamHandler.setProcessErrorStream(process.getErrorStream());
			streamHandler.start();
			return new Handle(process, streamHandler, endStreams::getStandardOutputContent, endStreams::getErrorOutputContent, endStreams::getOutputContent, () -> String.join(" ", processBuilder.command()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@RequiredArgsConstructor
	public static class Handle implements CommandLineToolExecutionHandle {
		private final Process process;
		private final PumpStreamHandler streamHandler;
		private final Supplier<CommandLineToolLogContent> standardOutput;
		private final Supplier<CommandLineToolLogContent> errorOutput;
		private final Supplier<CommandLineToolLogContent> output;
		private final Supplier<String> displayName;

		public CommandLineToolExecutionResult waitFor() {
			try {
				process.waitFor();
				streamHandler.stop();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		public CommandLineToolExecutionResult waitFor(long timeout, TimeUnit unit) {
			try {
				process.waitFor(timeout, unit);
				streamHandler.stop();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		public CommandLineToolExecutionResult waitFor(Duration duration) {
			try {
				process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS);
				streamHandler.stop();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), output.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
