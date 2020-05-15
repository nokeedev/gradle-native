package dev.nokee.core.exec;

import org.gradle.api.tasks.Internal;

public interface MetadataAwareCommandLineTool extends CommandLineTool {
	@Internal
	CommandLineToolMetadata getMetadata();
}
