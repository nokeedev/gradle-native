package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.Writer;
import java.nio.charset.Charset;

public class CommandLineToolInvocationStandardOutputRedirectForwardImpl implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal {
	private final Writer writer;

	public CommandLineToolInvocationStandardOutputRedirectForwardImpl(Writer writer) {
		this.writer = writer;
	}

	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(new TeeOutputStream(delegate.getStandardOutput(), new WriterOutputStream(writer, Charset.defaultCharset())), delegate.getErrorOutput());
	}
}
