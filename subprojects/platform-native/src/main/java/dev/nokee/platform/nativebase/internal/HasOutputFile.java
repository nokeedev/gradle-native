package dev.nokee.platform.nativebase.internal;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public interface HasOutputFile {
	Provider<RegularFile> getOutputFile();
}
