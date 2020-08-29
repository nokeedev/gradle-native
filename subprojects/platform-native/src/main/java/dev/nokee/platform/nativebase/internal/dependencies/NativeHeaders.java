package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;

public interface NativeHeaders {
	FileCollection getHeaderSearchPaths();
	FileCollection getFrameworkSearchPaths();
}
