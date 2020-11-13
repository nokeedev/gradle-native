package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.OutputStream;

public final class CommandLineToolInvocationStandardOutputRedirectForwardImpl implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal {
	private final OutputStream outputStream;

	public CommandLineToolInvocationStandardOutputRedirectForwardImpl(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(new TeeOutputStream(delegate.getStandardOutput(), outputStream), delegate.getErrorOutput());
	}
}
