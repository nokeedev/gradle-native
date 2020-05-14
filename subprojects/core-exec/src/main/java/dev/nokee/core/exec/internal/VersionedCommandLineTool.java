package dev.nokee.core.exec.internal;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.util.VersionNumber;

import java.io.File;

public class VersionedCommandLineTool extends DefaultMetadataAwareCommandLineTool {
	private final VersionNumber version;

	public VersionedCommandLineTool(File toolLocation, VersionNumber version) {
		super(toolLocation, new CommandLineToolVersionMetadata(version));
		this.version = version;
	}

	@Internal
	@Override
	public String getExecutable() {
		return super.getExecutable();
	}

	@Input
	protected String getVersion() {
		return version.toString();
	}
}
