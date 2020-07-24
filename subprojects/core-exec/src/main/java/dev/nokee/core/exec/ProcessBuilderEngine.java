package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationOutputRedirectInternal;
import dev.nokee.core.exec.internal.CommandLineToolOutputStreams;
import dev.nokee.core.exec.internal.DefaultCommandLineToolExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errStream = new ByteArrayOutputStream();

			var streams = new CommandLineToolOutputStreams(outStream, errStream);
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
			return new Handle(process, streamHandler, outStream::toString, errStream::toString, () -> String.join(" ", processBuilder.command()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@RequiredArgsConstructor
	public static class Handle implements CommandLineToolExecutionHandle {
		private final Process process;
		private final PumpStreamHandler streamHandler;
		private final Supplier<String> standardOutput;
		private final Supplier<String> errorOutput;
		private final Supplier<String> displayName;

		public CommandLineToolExecutionResult waitFor() {
			try {
				process.waitFor();
				streamHandler.stop();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), errorOutput.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
