package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.OutputStream;

public final class CommandLineToolInvocationErrorOutputRedirectForwardImpl implements CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal {
	private final OutputStream outputStream;

	public CommandLineToolInvocationErrorOutputRedirectForwardImpl(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(delegate.getStandardOutput(), new TeeOutputStream(delegate.getErrorOutput(), outputStream));
	}
}
