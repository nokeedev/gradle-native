package dev.nokee.publish.bintray.internal;

import org.gradle.api.file.Directory;

public interface StagingDirectory {
	default boolean exists() {
		return get().getAsFile().exists();
	}

	Directory get();
}
