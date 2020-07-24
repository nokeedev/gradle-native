package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.Serializable;

public class DuplicateToSystemOutputStreamCommandLineToolInvocationStandardOutputRedirect implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreams(new TeeOutputStream(delegate.getStandardOutput(), System.out), delegate.getErrorOutput());
	}
}
