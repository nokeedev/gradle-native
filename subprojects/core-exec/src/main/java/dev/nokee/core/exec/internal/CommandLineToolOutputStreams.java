package dev.nokee.core.exec.internal;

import java.io.OutputStream;

public interface CommandLineToolOutputStreams {
	OutputStream getStandardOutput();
	OutputStream getErrorOutput();
}
