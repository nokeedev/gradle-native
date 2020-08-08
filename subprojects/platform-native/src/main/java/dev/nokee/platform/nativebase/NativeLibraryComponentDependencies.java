package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.LibraryComponentDependencies;

/**
 * Allows the API and implementation dependencies of a native library to be specified.
 *
 * @since 0.5
 */
public interface NativeLibraryComponentDependencies extends LibraryComponentDependencies, NativeComponentDependencies, ComponentDependencies, NativeLibraryDependencies {
}
