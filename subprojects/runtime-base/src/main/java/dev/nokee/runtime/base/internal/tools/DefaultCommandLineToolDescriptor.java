package dev.nokee.runtime.base.internal.tools;

import lombok.Value;

import java.io.File;

@Value
public class DefaultCommandLineToolDescriptor implements CommandLineToolDescriptor {
	File path;
	String version;
}
