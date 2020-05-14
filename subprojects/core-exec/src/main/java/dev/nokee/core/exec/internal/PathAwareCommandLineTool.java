package dev.nokee.core.exec.internal;

import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.File;

public class PathAwareCommandLineTool extends DefaultCommandLineTool {
	private final File toolLocation;

	public PathAwareCommandLineTool(File toolLocation) {
		super(toolLocation);
		this.toolLocation = toolLocation;
	}

	@Internal
	@Override
	public String getExecutable() {
		return super.getExecutable();
	}

	@InputFile
	@PathSensitive(PathSensitivity.ABSOLUTE)
	protected File getInputFile() {
		return toolLocation;
	}
}
