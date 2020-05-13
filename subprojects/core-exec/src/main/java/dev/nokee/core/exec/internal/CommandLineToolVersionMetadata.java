package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolMetadata;
import lombok.RequiredArgsConstructor;
import org.gradle.util.VersionNumber;

@RequiredArgsConstructor
public class CommandLineToolVersionMetadata implements CommandLineToolMetadata {
	private final VersionNumber version;
}
