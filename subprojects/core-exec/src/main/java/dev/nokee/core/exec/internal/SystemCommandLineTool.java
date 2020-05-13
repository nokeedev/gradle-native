package dev.nokee.core.exec.internal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SystemCommandLineTool extends AbstractCommandLineTool {
	private final Object executable;

	@Override
	public String getExecutable() {
		return executable.toString();
	}
}
