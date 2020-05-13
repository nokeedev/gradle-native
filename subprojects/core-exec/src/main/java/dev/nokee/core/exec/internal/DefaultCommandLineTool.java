package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Arrays;

@RequiredArgsConstructor
public class DefaultCommandLineTool extends AbstractCommandLineTool {
	private final File toolLocation;

	@Override
	public String getExecutable() {
		return toolLocation.getAbsolutePath();
	}
}
