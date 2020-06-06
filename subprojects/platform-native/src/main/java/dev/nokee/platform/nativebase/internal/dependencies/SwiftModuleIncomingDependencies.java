package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.file.FileCollection;

public interface SwiftModuleIncomingDependencies {
	FileCollection getSwiftModules();
	FileCollection getFrameworkSearchPaths();
}
