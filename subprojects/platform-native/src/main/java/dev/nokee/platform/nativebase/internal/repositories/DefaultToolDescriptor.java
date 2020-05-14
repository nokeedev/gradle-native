package dev.nokee.platform.nativebase.internal.repositories;

import lombok.Value;

import java.io.File;

@Value
public class DefaultToolDescriptor implements ToolDescriptor {
	File path;
	String version;
}
