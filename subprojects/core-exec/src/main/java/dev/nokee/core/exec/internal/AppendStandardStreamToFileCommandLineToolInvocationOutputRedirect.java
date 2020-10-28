package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

@Value
public class AppendStandardStreamToFileCommandLineToolInvocationOutputRedirect implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationOutputRedirectInternal, Serializable {
	File file;

	@SneakyThrows
	@Override
	public CommandLineToolOutputStreams redirect(CommandLineToolOutputStreams delegate) {
		return new CommandLineToolOutputStreamsImpl(new TeeOutputStream(delegate.getStandardOutput(), new FileOutputStream(file)), delegate.getErrorOutput());
	}
}
