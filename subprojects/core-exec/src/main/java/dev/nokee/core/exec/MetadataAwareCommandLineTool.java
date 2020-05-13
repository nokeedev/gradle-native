package dev.nokee.core.exec;

public interface MetadataAwareCommandLineTool extends CommandLineTool {
	CommandLineToolMetadata getMetadata();
}
