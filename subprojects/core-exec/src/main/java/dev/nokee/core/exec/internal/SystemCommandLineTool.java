package dev.nokee.core.exec.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.tasks.Internal;

@RequiredArgsConstructor
public class SystemCommandLineTool extends AbstractCommandLineTool {
	private final Object executable;

	@Internal
	@Override
	public String getExecutable() {
		return executable.toString();
	}
}
