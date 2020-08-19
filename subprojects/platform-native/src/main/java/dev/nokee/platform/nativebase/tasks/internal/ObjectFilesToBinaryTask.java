package dev.nokee.platform.nativebase.tasks.internal;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;

public interface ObjectFilesToBinaryTask extends Task {
	@Internal
	Provider<RegularFile> getBinaryFile();

}
