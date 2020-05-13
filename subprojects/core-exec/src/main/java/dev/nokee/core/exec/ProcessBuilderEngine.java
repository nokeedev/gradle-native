package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.DefaultCommandLineToolExecutionResult;
import lombok.RequiredArgsConstructor;
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
		try {
			Process process = processBuilder.start();
			if (invocation.isCapturingStandardOutput()) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PumpStreamHandler streamHandler = new PumpStreamHandler(outStream);
				streamHandler.setProcessOutputStream(process.getInputStream());
				streamHandler.start();
				return new Handle(process, streamHandler, outStream::toString, () -> String.join(" ", processBuilder.command()));
			}
			throw new RuntimeException("Nop");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@RequiredArgsConstructor
	public static class Handle implements CommandLineToolExecutionHandle {
		private final Process process;
		private final PumpStreamHandler streamHandler;
		private final Supplier<String> standardOutput;
		private final Supplier<String> displayName;

		public CommandLineToolExecutionResult waitFor() {
			try {
				process.waitFor();
				streamHandler.stop();
				return new DefaultCommandLineToolExecutionResult(process.exitValue(), standardOutput.get(), displayName);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
