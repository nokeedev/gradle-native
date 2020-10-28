package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import lombok.Value;

import java.io.Serializable;

@Value
public class CommandLineToolInvocationOutputRedirectInheritImpl implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return delegate;
	}
}
