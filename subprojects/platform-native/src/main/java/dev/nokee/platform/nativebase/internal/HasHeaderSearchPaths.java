package dev.nokee.platform.nativebase.internal;

import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.util.Set;

public interface HasHeaderSearchPaths {
	Provider<Set<FileSystemLocation>> getHeaderSearchPaths();
}
