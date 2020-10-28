package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.Serializable;

public final class CommandLineToolInvocationErrorOutputRedirectDuplicateToSystemErrorStreamImpl implements CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(delegate.getStandardOutput(), new TeeOutputStream(delegate.getErrorOutput(), System.err));
	}
}
