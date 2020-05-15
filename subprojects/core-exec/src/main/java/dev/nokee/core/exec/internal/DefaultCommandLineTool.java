package dev.nokee.core.exec.internal;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.File;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class DefaultCommandLineTool extends AbstractCommandLineTool {
	private final File toolLocation;

	@Override
	public String getExecutable() {
		return toolLocation.getAbsolutePath();
	}
}
