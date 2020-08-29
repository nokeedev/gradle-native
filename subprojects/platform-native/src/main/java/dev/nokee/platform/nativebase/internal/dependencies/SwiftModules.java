package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;

public interface SwiftModules {
	FileCollection getSwiftModules();
	FileCollection getFrameworkSearchPaths();
}
