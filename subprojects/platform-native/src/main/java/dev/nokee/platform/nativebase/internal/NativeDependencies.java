package dev.nokee.platform.nativebase.internal;

import org.gradle.api.file.FileCollection;

public interface NativeDependencies {
	FileCollection getImportSearchPaths();
	FileCollection getHeaderSearchPaths();
	FileCollection getFrameworkSearchPaths();
	FileCollection getLinkLibraries();
	FileCollection getLinkFrameworks();
	FileCollection getRuntimeLibraries();
}
