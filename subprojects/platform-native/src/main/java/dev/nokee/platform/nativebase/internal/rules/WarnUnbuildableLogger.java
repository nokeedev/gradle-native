package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.ComponentIdentifier;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class WarnUnbuildableLogger {
	private static final Logger LOGGER = Logging.getLogger(WarnUnbuildableLogger.class);
	private final ComponentIdentifier<?> identifier;

	public WarnUnbuildableLogger(ComponentIdentifier<?> identifier) {
		this.identifier = identifier;
	}

	public void warn() {
		LOGGER.warn(String.format("'%s' component in project '%s' cannot build on this machine.", identifier.getName(), identifier.getProjectIdentifier().getPath()));
	}
}
