package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import org.gradle.api.file.FileCollection;

public interface NativeIncomingDependencies extends ResolvableComponentDependencies {
	FileCollection getHeaderSearchPaths();
	FileCollection getFrameworkSearchPaths();
	FileCollection getSwiftModules();
	FileCollection getLinkLibraries();
	FileCollection getLinkFrameworks();
	FileCollection getRuntimeLibraries();
}
