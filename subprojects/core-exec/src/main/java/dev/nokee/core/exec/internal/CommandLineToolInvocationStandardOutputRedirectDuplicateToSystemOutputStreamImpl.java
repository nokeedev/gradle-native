package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.Serializable;

public final class CommandLineToolInvocationStandardOutputRedirectDuplicateToSystemOutputStreamImpl implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(new TeeOutputStream(delegate.getStandardOutput(), System.out), delegate.getErrorOutput());
	}
}
