package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolMetadata;
import dev.nokee.core.exec.MetadataAwareCommandLineTool;

import java.io.File;

public class DefaultMetadataAwareCommandLineTool extends DefaultCommandLineTool implements MetadataAwareCommandLineTool {
	private final CommandLineToolMetadata metadata;

	public DefaultMetadataAwareCommandLineTool(File toolLocation, CommandLineToolMetadata metadata) {
		super(toolLocation);
		this.metadata = metadata;
	}

	@Override
	public CommandLineToolMetadata getMetadata() {
		return metadata;
	}
}
