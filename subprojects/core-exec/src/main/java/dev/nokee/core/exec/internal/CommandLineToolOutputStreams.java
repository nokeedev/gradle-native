package dev.nokee.core.exec.internal;

import lombok.Value;

import java.io.OutputStream;

@Value
public class CommandLineToolOutputStreams {
	OutputStream standardOutput;
	OutputStream errorOutput;
}
