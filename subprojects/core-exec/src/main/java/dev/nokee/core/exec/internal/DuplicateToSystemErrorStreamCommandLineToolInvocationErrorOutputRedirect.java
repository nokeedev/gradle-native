package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.Serializable;

public class DuplicateToSystemErrorStreamCommandLineToolInvocationErrorOutputRedirect implements CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreams(delegate.getStandardOutput(), new TeeOutputStream(delegate.getErrorOutput(), System.err));
	}
}
