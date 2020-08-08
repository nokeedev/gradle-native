package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;

public interface NativeIncomingDependencies {
	FileCollection getHeaderSearchPaths();
	FileCollection getFrameworkSearchPaths();
	FileCollection getSwiftModules();
	FileCollection getLinkLibraries();
	FileCollection getLinkFrameworks();
	FileCollection getRuntimeLibraries();
}
