package dev.nokee.platform.ios.internal;

import lombok.Value;

import java.io.File;

@Value
public class CommandLineToolDescriptor {
	File path;
	String version;
}
