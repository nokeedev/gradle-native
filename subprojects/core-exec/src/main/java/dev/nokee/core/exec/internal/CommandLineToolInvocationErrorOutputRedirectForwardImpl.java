package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.Writer;
import java.nio.charset.Charset;

public final class CommandLineToolInvocationErrorOutputRedirectForwardImpl implements CommandLineToolInvocationErrorOutputRedirect, CommandLineToolInvocationOutputRedirectInternal {
	private final Writer writer;

	public CommandLineToolInvocationErrorOutputRedirectForwardImpl(Writer writer) {
		this.writer = writer;
	}

	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(delegate.getStandardOutput(), new TeeOutputStream(delegate.getErrorOutput(), new WriterOutputStream(writer, Charset.defaultCharset())));
	}
}
