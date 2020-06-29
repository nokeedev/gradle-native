package dev.nokee.platform.nativebase;

import dev.nokee.platform.nativebase.tasks.LinkBundle;
import org.gradle.api.tasks.TaskProvider;

/**
 * A bundle built from 1 or more native language.
 *
 * @since 0.4
 */
public interface BundleBinary extends NativeBinary {
	/**
	 * Returns a provider for the task that links the object files into this binary.
	 *
	 * @return a provider of {@link LinkBundle} task, never null.
	 */
	TaskProvider<LinkBundle> getLinkTask();
}
