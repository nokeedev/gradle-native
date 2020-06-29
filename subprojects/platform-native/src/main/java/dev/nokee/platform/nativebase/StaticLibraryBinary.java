package dev.nokee.platform.nativebase;

import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import org.gradle.api.tasks.TaskProvider;

/**
 * A static library built from 1 or more native language.
 *
 * @since 0.4
 */
public interface StaticLibraryBinary extends NativeBinary {
	/**
	 * Returns a provider for the task that creates the static archive from the object files.
	 *
	 * @return a provider of {@link CreateStaticLibrary} task, never null.
	 */
	TaskProvider<CreateStaticLibrary> getCreateTask();
}
